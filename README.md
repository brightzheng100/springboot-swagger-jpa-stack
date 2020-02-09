# springboot-swagger-jpa-stack

It's a common practice to start a Java project with some goals in mind.

If you want to build RESTful APIs with DB as the backend, you may think of:

- Using [Spring Boot](http://projects.spring.io/spring-boot/) because it brings in great productivity and simplifies Java development;
- Using [Swagger](https://github.com/swagger-api/swagger-ui) so make exposing and documenting RESTful APIs fun;
- Using JPA because it's standard and makes talking to RDBMS easy;
- Using [H2](https://www.h2database.com) as an embedded database for local development and testing;
- Using [Flyway](https://flywaydb.org/) to make database upgrades / patches as code;
- Using [micrometer](http://micrometer.io/) to expose Prometheus-style metrics;
- Using Spring Boot Test Framework with Junit, [Rest Assured](https://github.com/rest-assured/rest-assured) for testing.
- etc.

But make all these frameworks fully integrated may take some effort.

So the aim of this sample Java project is to integrate them with some practices and sample code for you to start with.

There is also a bonus: a workable `Dockerfile` which supports multi-stage!

The major components include:

- **Spring Boot v2.2.x**
  - org.springframework.boot:spring-boot-starter-web
  - org.springframework.boot:spring-boot-starter-data-jpa
  - org.springframework.boot:spring-boot-starter-log4j2
  - org.springframework.boot:spring-boot-starter-actuator
- **Swagger v2.9.x**
  - io.springfox:springfox-swagger2
  - io.springfox:springfox-swagger-ui
- **Flyway v6.0.x**
  - org.flywaydb:flyway-core
- **H2 v1.4.x**
  - com.h2database:h2
- **Prometheus support with micrometer**
  - io.micrometer:micrometer-registry-prometheus
- **Testing**
  - org.springframework.boot:spring-boot-starter-test
  - junit:junit
  - io.rest-assured:rest-assured


## File Structure

The project follows a series of conventions / best practices of Maven, Spring Boot and Flyway.

```
$ tree .
.
├── Dockerfile
├── LICENSE
├── README.md
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── app
    │   │       ├── Application.java
    │   │       ├── config
    │   │       │   └── SwaggerConfig.java
    │   │       ├── controller
    │   │       │   ├── DefaultController.java
    │   │       │   ├── GreetingController.java
    │   │       │   └── StudentController.java
    │   │       ├── model
    │   │       │   ├── Greeting.java
    │   │       │   └── Student.java
    │   │       └── repository
    │   │           └── StudentRepository.java
    │   └── resources
    │       ├── application-default.yml
    │       ├── application-prod.yml
    │       ├── application.yml
    │       ├── bootstrap.yml
    │       └── db
    │           └── migration
    │               ├── V1.0.0__initial.sql
    │               ├── V1.0.1__add_person.sql
    │               └── V1.0.2__alter_student.sql
    └── test
        └── java
            └── app
                └── ApplicationTests.java
```


## Get Started

Make sure you have Maven 3.x and JDK 1.8+ installed.

```
$ java -version
openjdk version "1.8.0_242"
OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_242-b08)
OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.242-b08, mixed mode)

$ mvn -version
Apache Maven 3.6.2 ...
```

Then let's get started:

```
$ mvn clean spring-boot:run
```

> Note: 
> 1. Without specifying any profile, it will automatically fall back to `default` profile so `application.yml` and `application-default.yml` will be loaded, where `H2` embedded in-memory will be used;
> 2. The app serves at port `8080`, as usual.


## Play With It

### Access Swagger UI

Open a browser and navigate to: http://localhost:8080/swagger-ui.html

```sh
$ open http://localhost:8080/swagger/index.html
```

![swagger-ui](misc/screenshot-swagger.png "Swagger UI")

> Note: All the APIs exposed can be tried through the UI, do try it out!


### See What Actuator Offers

A list out-of-the-box Actuator services have been exposed, in `dev` profile.

![actuator-services](misc/screenshot-actuator-services.png "Actuator Services")

The `health` with DB info: http://localhost:8080/actuator/health

![actuator-health](misc/screenshot-actuator-health.png "Actuator health")

The `Flyway` info: http://localhost:8080/actuator/flyway

![actuator-flyway](misc/screenshot-actuator-flyway.png "Actuator flyway")

The Prometheus metrics: http://localhost:8080/actuator/prometheus

![actuator-prometheus](misc/screenshot-actuator-prometheus.png "Actuator prometheus")


### Access H2 Console

Open a browser and navigate to: http://localhost:8080/h2-console

![h2-login](misc/screenshot-h2-login.png "H2 Login")

Make sure:
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **User Name**: `sa`
- **Password** is empty.

Click the Connect button and we can play with the database.

![h2-main](misc/screenshot-h2-main.png "H2 Main")


### Run With A Specified Spring Profile

By default, it will load `H2` as the embedded in-memory DB for devevelopment and testing purposes.
But you may want to connect to **production**-like external DB, e.g. MySQL, without changing the code.

In this case, we can specify the Spring profile where you configure the right DB backend at run time.
This project includes a `application-prod.yml` file, as an example, where we specify MySQL DB as our backend.

Let's assume that the MySQL Server has been installed in our working machine fullfiled what we have set in `application-prod.yml` file:
- url: `jdbc:mysql://<DB HOST>:3306/testdb`
- username: `myuser`
- password: `mypasswd`

Firstly, let's log into MySQL and create the database:

```sh
$ mysql -u root -p

$ mysql> create database testdb;
```

And then open another console to start the application:

```sh
$ SPRING_PROFILES_ACTIVE=prod mvn clean spring-boot:run
```

> Note: 
> 1. The application will automatically activate `prod` and `mysql` profiles where MySQL will be used;
> 2. The schema and data will be created automatically at first time and fully managed by `Flyway`.


### Containerize & Run It

A `Dockerfile` is provided for you to containerize the app with multi-stage build in mind.

```sh
$ docker build -t mydockerhubaccount/springboot-swagger-jpa .
```

There are a couple of build args provided, with defaults:

| ARG | Default | Purposes  |
| --- | --- | --- |
| ARTIFACT_TITLE  | "springboot-swagger-jpa-stack" | To tag the Docker image title: `LABEL org.opencontainers.image.title="${ARTIFACT_TITLE}"` |
| ARTIFACT_VERSION  | "1.0.0-SNAPSHOT" | To tag the Docker image version: `LABEL org.opencontainers.image.version="${ARTIFACT_VERSION}"` |

For example, if we want to build the Image tagged as:
- org.opencontainers.image.title=my-app
- org.opencontainers.image.version=1.0.0

```
$ docker build \
  --build-arg ARTIFACT_TITLE="my-app" \
  --build-arg ARTIFACT_VERSION="1.0.0" \
  -t mydockerhubaccount/springboot-swagger-jpa:1.0.0 .
```

Meanwhile, there are also env variables for run time:

| ENV | Default | Purposes  |
| --- | --- | --- |
| JVM_ARGS  | "" | To tune the JVM args if there is a need |

Please note that we can always inject the Spring-powered environment variables as well to influence some desired behaviours.

So to run it with Docker's ENV `JVM_ARGS` and Spring variables `SPRING_PROFILES_ACTIVE`, `SPRING_DATASOURCE_URL`, we can do this:

```
$ docker run \
  -e "JVM_ARGS=-Xms2G -Xmx2G" \
  -e "SPRING_PROFILES_ACTIVE=prod" \
  -e 'SPRING_DATASOURCE_URL=jdbc:mysql://192.168.1.148:3306/testdb' \
  -p "8080:8080" \
  mydockerhubaccount/springboot-swagger-jpa:1.0.0
...
==> [INFO] Application(655) - The following profiles are active: prod,mysql
...
==> [INFO] TomcatWebServer(204) - Tomcat started on port(s): 8080 (http) with context path ''
...
```

> Note: 
> 1. The MySQL IP of `192.168.1.148` is my laptop's IP, change it accordingly to yours;
> 2. You may inject more Spring variables, if there is a need, to override the default values;
> 3. Change the **`mydockerhubaccount`** to yours, or use your image naming pattern instead.


## References

- Spring Boot with Docker: https://spring.io/guides/gs/spring-boot-docker/
- Spring Data JPA Doc: https://docs.spring.io/spring-data/jpa/docs/2.2.4.RELEASE/reference/html/#jpa.query-methods.query-creation
- Some Dockerfile experience: https://github.com/appsody/stacks/tree/master/incubator/java-spring-boot2

