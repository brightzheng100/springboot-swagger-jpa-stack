## Simple experiment with Instana programmatic SDK

Instana offers auto-instrumentation for Java apps, among other languages.
So there is zero-touch while enjoying the tracing, by default.

Meanwhile, there is also a configuration-based Java SDK by which we can further enhance the tracing context by configuration, without a need for code changes.


### Why am I doing this?

Well, this is just a simple experiment for how to use Instana SDK programmatically.


### The code changes

In `pom.xml`, define a version variable:

```xml
<instana-java-sdk.version>1.2.0</instana-java-sdk.version>
```

And then add the SDK as the dependency:

```xml
		<!-- Instana SDK -->
		<dependency>
			<groupId>com.instana</groupId>
			<artifactId>instana-java-sdk</artifactId>
			<version>${instana-java-sdk.version}</version>
		</dependency>
```

All Instana related code is within one controller, which is [src/main/java/app/controller/SpansController.java](src/main/java/app/controller/SpansController.java).


### Agent configuration

We should enable the `javatrace` while disabling the default auto-instrumentation:

```sh
cat <<EOF | sudo tee /opt/instana/agent/etc/instana/configuration-javatrace.yaml
com.instana.plugin.javatrace:
  instrumentation:
    enabled: true
    sdk:
      packages:
        - 'app.controller'
EOF
```

It's recommended to configure the zone, if haven't:

```sh
INSTANA_ZONE="Student-0-Zone" && \
cat <<EOF | sudo tee /opt/instana/agent/etc/instana/configuration-zone.yaml
com.instana.plugin.generic.hardware:
  enabled: true
  availability-zone: "${INSTANA_ZONE}"
EOF
```

### Run the App

```sh
kill -9 $(cat app.pid)
nohup bash -c "mvn spring-boot:run" &> app.out & echo $! > app.pid
```

You may have a quick test for different endpoints:

```sh
curl http://localhost:8080/api/v1/spans
curl http://localhost:8080/api/v1/spans/1
curl http://localhost:8080/api/v1/spans/error
curl http://localhost:8080/api/v1/students
curl http://localhost:8080/api/v1/students/10001
curl http://localhost:8080/api/v1/httpbin/get
curl -X POST http://localhost:8080/api/v1/httpbin/post
```

### Load some traffic

If it works, you may load some traffic too:

```sh
kill -9 $(cat load.pid)
nohup bash -c "./load-gen.sh" &> load.out & echo $! > load.pid
```
