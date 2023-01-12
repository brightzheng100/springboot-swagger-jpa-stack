# OpenTelemetry with Instana

Instana's agent will act as the [Collector](https://opentelemetry.io/docs/collector/) to ingest the observability data and forward it to the backend.

So the route would be: Application -> Instana Host Agent -> Instana Backend.

We assume that below software has been properly installed and configured in this host for running our demo:
- JDK 1.8+
- Maven 3.3+


## Install Instana Host Agent

Instana agent can be installed almost everywhere into your manage-to footprint.

You may simply generate the one-liner installation script from Instana UI and run it in your host to install.


## Enable Instana Agent's OpenTelemetry Sensor

The OpenTelemetry sensor is disabled by default.

We can enable that by adding a new configuration file which will be automatically hot reloaded by the agent:

```sh
cat <<EOF | sudo tee /opt/instana/agent/etc/instana/configuration-zone.yaml
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


## Point App's OpenTelemetry Endpoint to Instana Host Agent

```sh
export OTEL_SERVICE_NAME=springboot-app
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317

git clone https://github.com/brightzheng100/springboot-swagger-jpa-stack
cd springboot-swagger-jpa-stack

wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Build it, if not yet
mvn clean package

# Run it
java -jar \
  -javaagent:`pwd`/opentelemetry-javaagent.jar \
  -Dspring.profiles.active=otel \
  target/springboot-swagger-jpa-stack-1.0.0-SNAPSHOT.jar

mvn clean spring-boot:run \
  -Dspring-boot.run.profiles=otel \
  -Dspring-boot.run.jvmArguments="-javaagent:`pwd`/opentelemetry-javaagent.jar"
```

## Let's generate some traffic

```sh
./load-gen.sh
```

## Discover things in Instana

TODO
