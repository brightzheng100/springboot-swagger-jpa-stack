# OpenTelemetry Experiments

## Any Code Changes Needed?

What I'd experiment is the automatic instrumentation offered by OpenTelemetry's sub project named [`opentelemetry-java-instrumentation`](https://github.com/open-telemetry/).
In this case, there are no specific code changes.

But there are a few things should be highlighed:
- For metrics and tracing, simply adding the `-javaagent:<path-to>/opentelemetry-javaagent.jar` will just work;
- For logging, where the support is still in early days, there is a way to auto instrument polular logging frameworks like [Log4j](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/86961d496ade8a1876e9548af9c391a0645ce649/instrumentation/log4j/log4j-2.13.2/library/README.md), among others.

We don't need to do things very specificially for metrics and tracing, but we need to add some more dependencies for logging.
And as of now, what I've added are:
```xml
		<dependency>
			<groupId>io.opentelemetry.instrumentation</groupId>
			<artifactId>opentelemetry-log4j-2.13.2</artifactId>
			<version>1.9.2-alpha</version>
			<scope>runtime</scope>
		</dependency>
```

## When Running in non-K8s environment, like a VM

Let's try it out step by step:

1. Download and start the `otelcol` tool for testing purposes:

```sh
# Download the right otelcol binnary
wget https://github.com/open-telemetry/opentelemetry-collector-releases/releases/download/v0.69.0/otelcol_0.69.0_darwin_arm64.tar.gz
tar -xvf otelcol_0.69.0_darwin_arm64.tar.gz

./otelcol -v
otelcol version 0.69.0

# Start it up
./otelcol --config otel-collector.yaml
```

2. Open a new console, download the OpenTelemetry's Java agent, and start it with the agent:

```sh
wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=otlp
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:55690

mvn spring-boot:run \
  -Dspring-boot.run.profiles=otel,h2 \
  -Dspring-boot.run.jvmArguments="-javaagent:`pwd`/opentelemetry-javaagent.jar"
```

3. Open your browse and navigate to: http://localhost:8080/

You may try any of the APIs, say this one: http://localhost:8080/swagger-ui/index.html#/student-controller/listAllUsingGET, and you will see a lot of detailed info from both `otelcol` and app's console.
Most importantly, in `otelcol`'s console, we can see metrics, tracing and log exporters are working; and in our app's console, you will see the Log4j has been auto instrumented, and every log entity has injected `traceId` and `spanId` values, without any code changes, like this:

```log
{"timestamp":"2023-01-19T17:17:20.622+0800","thread.name":"http-nio-8080-exec-2","log.level":"INFO","logger.name":"app.controller.StudentController","message":"PUT v1/students/","trace_id":"5c1e3fe428f83f7d9d1a82970fa2e36b","span_id":"59efdfddd252036a","service.name":"springboot-app"}
```

> Note: I used the "org.apache.logging.log4j:log4j-layout-template-json" library for formatting OTEL log entities here.

It would be even cooler if we export all this observability data to an observability platform like [Instana](https://instana.com).
Please refer to [README-OTEL-INSTANA](README-OTEL-INSTANA.md) for the detailed experiments.


## When running in Kubernetes

### Preparation / Prerequisites

1. Cert Manager is installed (in default `cert-manager` namespace for example).

If not yet, do this:

```sh
# https://cert-manager.io/docs/installation/
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml

# Run these two commands ONLY if you're with OpenShift
oc adm policy add-scc-to-user anyuid -z cert-manager -z cert-manager-cainjector -z cert-manager-webhook -n cert-manager
oc adm policy add-scc-to-user privileged -z cert-manager -z cert-manager-cainjector -z cert-manager-webhook -n cert-manager
```

2. Install OpenTelemetry Operator

```sh
kubectl apply -f https://github.com/open-telemetry/opentelemetry-operator/releases/latest/download/opentelemetry-operator.yaml
```

### Let's do these for our app

1. Assuming we're working with our dedicated namespace, say `demo`.

Or create such a namespace:

```sh
kubectl create namespace demo
```

2. Create OpenTelemetry Collector (otelcol)

```sh
# Create a very simple demo Collector: otlp (receivers) -> logging (exporters)
$ kubectl apply -n demo -f - <<EOF
apiVersion: opentelemetry.io/v1alpha1
kind: OpenTelemetryCollector
metadata:
  name: demo-collector
spec:
  #mode: Deployment # DaemonSet, Sidecar, or Deployment (default)
  config: |
    receivers:
      otlp:
        protocols:
          grpc:
          http:

    processors:

    exporters:
      logging:
        verbosity: detailed # detailed|normal|basic)

    service:
      pipelines:
        traces:
          receivers: [otlp]
          processors: []
          exporters: [logging]
        metrics:
          receivers: [otlp]
          processors: []
          exporters: [logging]
        logs:
          receivers: [otlp]
          processors: []
          exporters: [logging]
EOF

$ kubectl get svc -n demo
NAME                                  TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)                       AGE
demo-collector-collector              ClusterIP   10.43.229.42   <none>        4317/TCP,4318/TCP,55681/TCP   4m23s
demo-collector-collector-headless     ClusterIP   None           <none>        4317/TCP,4318/TCP,55681/TCP   4m23s
demo-collector-collector-monitoring   ClusterIP   10.43.40.185   <none>        8888/TCP                      4m23s
```

3. Create the OpenTelemetry auto-instrumentation injection 

```sh
kubectl apply -n demo -f - <<EOF
apiVersion: opentelemetry.io/v1alpha1
kind: Instrumentation
metadata:
  name: demo-instrumentation
spec:
  exporter:
    endpoint: http://demo-collector-collector:4317  # let's use the gRPC svc endpoint here
EOF
```

> Note: auto-instrumentation can be applied to namespace too by adding the annotation into the namespace. But it's not very suitable for our demo.
```
kubectl annotate namespace demo instrumentation.opentelemetry.io/inject-java="true"
```

3. Create app's objects

```sh
# Clone the project for the provided manifests
$ git clone https://github.com/brightzheng100/springboot-swagger-jpa-stack.git
$ cd springboot-swagger-jpa-stack

# Define where to look up for your Docker image, or you can simply use mine
$ export IMAGE_NAMESPACE=docker.io/brightzheng100
# The database URL, which in our case is also within K8s/OCP, but it can be external too
$ export DATASOURCE_URL=jdbc:mysql://mysql:3306/testdb

# For x86_64 or amd64
$ export IMAGE_MYSQL=mysql:5.7 && export IMAGE_APP=springboot-swagger-jpa-stack:latest
# For ARM64
$ export IMAGE_MYSQL=arm64v8/mysql && export IMAGE_APP=springboot-swagger-jpa-stack:arm64

# Deploy the objects
# secrets
$ kubectl apply -f kubernetes/secret.yaml -n demo
# mysql
$ envsubst < kubernetes/mysql.yaml | kubectl apply -f - -n demo
# with otel auto instrumentation annotation:
#   annotations:
#     instrumentation.opentelemetry.io/inject-java: "true"  # newly added for auto instrumentatioin support
$ envsubst < kubernetes/app-with-otel.yaml | kubectl apply -f - -n demo

# Run the load-gen too, to generate traffic for tracing
$ kubectl apply -f kubernetes/load-gen.yaml -n demo
```

### Observe the results

#### The auto instrumentation

The OpenTelemetry's auto-instrumentation will kick in automatically.

Some extra OTEL-related environment variables, among others, will be automatically injected:

```
Environment:
  ...
  JAVA_TOOL_OPTIONS:                    -javaagent:/otel-auto-instrumentation/javaagent.jar
  OTEL_SERVICE_NAME:                   springboot-swagger-jpa-stack
  OTEL_EXPORTER_OTLP_ENDPOINT:         http://demo-collector-collector:4317
  OTEL_RESOURCE_ATTRIBUTES_POD_NAME:   springboot-swagger-jpa-stack-89f5b665f-mdcgv (v1:metadata.name)
  OTEL_RESOURCE_ATTRIBUTES_NODE_NAME:   (v1:spec.nodeName)
  OTEL_RESOURCE_ATTRIBUTES:            k8s.container.name=springboot-swagger-jpa-stack,k8s.deployment.name=springboot-swagger-jpa-stack,k8s.namespace.name=demo,k8s.node.name=$(OTEL_RESOURCE_ATTRIBUTES_NODE_NAME),k8s.pod.name=$(OTEL_RESOURCE_ATTRIBUTES_POD_NAME),k8s.replicaset.name=springboot-swagger-jpa-stack-89f5b665f
```

#### The logs

We may tail the logs of 3 Pods: 
- The OpenTelemetry Operator Pod in `opentelemetry-operator-system` namespace;
- The Collector Pod in the `demo` namespace; and
- The application Pod in the `demo` namespace.

And you should be able to see a lot of interesting output from the consoles, like below:
![OpenTelemetry](misc/screenshot-otel.png "OpenTelemetry Outputs")
