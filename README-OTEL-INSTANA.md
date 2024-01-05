# OpenTelemetry with Instana

Instana's agent will act as the [Collector](https://opentelemetry.io/docs/collector/) to ingest the observability data and forward it to the backend.
You may take a look at how OpenTelemetry works with this demo application too, [here](./README-OTEL.md), without Instana.

The route when integrating with Instana would be: Applications -> Instana Host Agent -> Instana Backend.

## Running the demo app in non-Kubernetes env, like a VM

We assume that below software components have been properly installed and configured in this host for running our demo:
- JDK 1.8+
- Maven 3.3+

### Any changes needed from the application perspective?

In short, no.

Please refer to [README-OTEL.md](./README-OTEL.md) for details.


### Install Instana Host Agent

Instana agent can be installed almost everywhere into your manage-to footprint.

You may simply generate the one-liner installation script from Instana UI and run it in your host to install.


### Enable Instana Agent's OpenTelemetry Sensor

The OpenTelemetry sensor is disabled by default.

We can enable that by adding a new configuration file which will be automatically hot reloaded by the agent:

```sh
cat <<EOF | sudo tee /opt/instana/agent/etc/instana/configuration-otel.yaml
com.instana.plugin.opentelemetry:
  grpc:
    enabled: true   # grpc endpoints
  http:
    enabled: true   # http endpoints
EOF
```

> Note: Instana's OpenTelemetry sensor serves gRPC endpoints at port `4317` and HTTP(S) endpoints at port `4318`.

And we need to disable Instana Agent's auto instrumentation so we use OpenTelemetry's machenism only:

```sh
cat <<EOF | sudo tee /opt/instana/agent/etc/instana/configuration-javatrace.yaml
com.instana.plugin.javatrace:
  instrumentation:
    enabled: false
EOF

# Currently, disabling Java auto instrumentation requires a restart of Instana Agent
sudo systemctl restart instana-agent
```


### Point App's OpenTelemetry Endpoint to Instana Host Agent

```sh
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317

git clone https://github.com/brightzheng100/springboot-swagger-jpa-stack
cd springboot-swagger-jpa-stack

wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Build it, if not yet
mvn clean package

# Run it
# By Maven
mvn clean spring-boot:run \
  -Dspring-boot.run.profiles=otel \
  -Dspring-boot.run.jvmArguments="-javaagent:`pwd`/opentelemetry-javaagent.jar"
# Or simply by java
java -jar \
  -javaagent:`pwd`/opentelemetry-javaagent.jar \
  target/springboot-swagger-jpa-stack-1.0.0-SNAPSHOT.jar
```

### Let's generate some traffic

```sh
./load-gen.sh
```

## Running the demo app in Kubernetes

### Any changes needed from the application perspective?

In short, no.

Please refer to [README-OTEL.md](./README-OTEL.md) for details.

But there is one thing to note when you build your Docker image.
As the OpenTelemetry's auto-instrumentation will automatically do a series of things:
- download and 
- expose 

with otel auto instrumentation annotation:
   annotations:
     instrumentation.opentelemetry.io/inject-java: "true"  # newly added for auto instrumentatioin support

### Install Instana Agent

You may simply generate the one-liner installation script from Instana UI and run it in your host to install.

For simplicity, I prefer `helm` and please note that we need to customize the command based on the command generated from Instana UI, by explicitly setting the `agent.configuration_yaml` to disable the Javatrace while enabling OpenTelemetry support: 

```sh
helm install instana-agent \
   --repo https://agents.instana.io/helm \
   --namespace instana-agent \
   --create-namespace \
   --set agent.key=xxxxx \
   --set agent.downloadKey=xxxx \
   --set agent.endpointHost=ingress-orange-saas.instana.io \
   --set agent.endpointPort=443 \
   --set cluster.name='bright-cluster' \
   --set zone.name='Bright-Zone' \
   --set agent.configuration_yaml="
# Java auto instrumentation by Instana Autotrace
com.instana.plugin.javatrace:
  instrumentation:
    enabled: false  # disable it as we'd use OTEL's auto instrumentation

# OpenTelemetry Sensor
com.instana.plugin.opentelemetry:
  grpc:
    enabled: true   # grpc endpoints
  http:
    enabled: true   # http endpoints
" \
   instana-agent
```

### Create/update a service for exposing the agent ports

```sh
kubectl apply -n instana-agent -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: instana-agent
spec:
  ports:
  - name: agent-apis
    port: 42699
    protocol: TCP
    targetPort: 42699
  - name: opentelemetry
    port: 55680
    protocol: TCP
    targetPort: 55680
  - name: opentelemetry-grpc
    port: 4317
    protocol: TCP
    targetPort: 4317
  - name: opentelemetry-http
    port: 4318
    protocol: TCP
    targetPort: 4318
  selector:
    app.kubernetes.io/instance: instana-agent
    app.kubernetes.io/name: instana-agent
EOF
```

### Create the OpenTelemetry auto-instrumentation injection

OpenTelemetry offers auto-instrumentation for a series of languages, including Java.

So to enable that in Kubernetes, we need to install the OpenTelemetry Operator and create a `Instrumentation` CR.

```sh
# Install the Cert Manager, if you haven't, as the prerequisite
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml

# Run these two commands ONLY if you're with OpenShift
oc adm policy add-scc-to-user anyuid -z cert-manager -z cert-manager-cainjector -z cert-manager-webhook -n cert-manager
oc adm policy add-scc-to-user privileged -z cert-manager -z cert-manager-cainjector -z cert-manager-webhook -n cert-manager

# Install OpenTelemetry Operator
kubectl apply -f https://github.com/open-telemetry/opentelemetry-operator/releases/latest/download/opentelemetry-operator.yaml
```

### Create a dedicated namespace for our demo

```sh
kubectl create namespace demo
```

Please note that the instrumentation can be applied to namespace level too if we annotate the namespace properly.
But we're NOT going to do this as once we do this, ALL Pods will be auto instrumented.

```sh
kubectl annotate namespace demo instrumentation.opentelemetry.io/inject-java="true"
```

### Create the Instrumentation CR

Instana's OpenTelemetry Sensor listens on port 4317 for gRPC protocol, which will become the OTEL endpoint that our apps report to.

```sh
kubectl apply -n demo -f - <<EOF
apiVersion: opentelemetry.io/v1alpha1
kind: Instrumentation
metadata:
  name: demo-instrumentation
spec:
  env:
  - name: OTEL_INSTANA_AGENT_HOST
    valueFrom:
      fieldRef:
        fieldPath: status.hostIP
  exporter:
    endpoint: http://instana-agent.instana-agent.svc:4317  # let's use the gRPC svc endpoint here
EOF
```

### Create app's objects

1. Clone the project for the provided manifests:

```sh
git clone https://github.com/brightzheng100/springboot-swagger-jpa-stack.git
cd springboot-swagger-jpa-stack
```

2. Expose proper vars

```sh
export IMAGE_NAMESPACE=docker.io/brightzheng100
export DATASOURCE_URL=jdbc:mysql://mysql:3306/testdb

# For x86_64 or amd64
export IMAGE_MYSQL=mysql:8 && export IMAGE_APP=springboot-swagger-jpa-stack:latest
# For ARM64
export IMAGE_MYSQL=arm64v8/mysql && export IMAGE_APP=springboot-swagger-jpa-stack:arm64
```

3. Deploy the objects:

```sh
kubectl apply -f kubernetes/secret.yaml -n demo
envsubst < kubernetes/mysql.yaml | kubectl apply -f - -n demo
envsubst < kubernetes/app-with-otel.yaml | kubectl apply -f - -n demo
```

4. Run the load-gen too, to generate traffic for tracing

```sh
kubectl apply -f kubernetes/load-gen.yaml -n demo
```

## Discover things in Instana

```yaml
com.instana.plugin.opentelemetry:
  enabled: true # legacy setting, will only enable grpc, defaults to false
```

