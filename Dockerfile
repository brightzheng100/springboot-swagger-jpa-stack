### COMPILE PHASE

FROM maven:3.6.3-jdk-11-openj9 as compile

# Envs
ENV PROJECT_DIR /project

# Ensure up to date / patched OS
RUN  apt-get -qq update \
  && DEBIAN_FRONTEND=noninteractive apt-get -qq upgrade -y \
  && apt-get -qq clean \
  && rm -rf /tmp/* /var/lib/apt/lists/*

# Create non-root user / group to run
RUN  groupadd --gid 1000 java_group \
  && useradd --uid 1000 --gid java_group --shell /bin/bash --create-home java_user \
  && mkdir -p /mvn/repository && chown -R java_user:java_group /mvn \
  && mkdir $PROJECT_DIR && chown -R java_user:java_group $PROJECT_DIR

# Switch to non-root user and workdir
USER java_user:java_group
WORKDIR $PROJECT_DIR/

# Copy files
COPY --chown=java_user:java_group src $PROJECT_DIR/src/
COPY --chown=java_user:java_group pom.xml $PROJECT_DIR

# Package it
RUN mvn --no-transfer-progress clean package

# Extract the jar
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)


#### BUILD PHASE

FROM adoptopenjdk:11-jdk-openj9

# Build args
ARG SPRING_PROFILE="dev"
ARG ARTIFACT_ID=springboot-swagger-jpa-stack
ARG ARTIFACT_VERSION=1.0.0-SNAPSHOT
ARG JVM_ARGS=""

# Envs
ENV APP_DIR /app
ENV DEPENDENCY=/project/target/dependency

# Ensure up to date / patched OS
RUN  apt-get -qq update \
  && DEBIAN_FRONTEND=noninteractive apt-get -qq upgrade -y \
  && apt-get -qq clean \
  && rm -rf /tmp/* /var/lib/apt/lists/*

# Create non-root user / group to run
RUN groupadd --gid 1000 java_group \
  && useradd --uid 1000 --gid java_group --shell /bin/bash --create-home java_user \
  && mkdir $APP_DIR && chown -R java_user:java_group $APP_DIR

# Switch to non-root user and workdir
USER java_user:java_group
WORKDIR $APP_DIR

# Label the image
LABEL org.opencontainers.image.title="${ARTIFACT_ID}"
LABEL org.opencontainers.image.version="${ARTIFACT_VERSION}"

# Layering the app instead of using the fat jar 
COPY --from=compile ${DEPENDENCY}/BOOT-INF/lib $APP_DIR/lib
COPY --from=compile ${DEPENDENCY}/META-INF $APP_DIR/META-INF
COPY --from=compile ${DEPENDENCY}/BOOT-INF/classes $APP_DIR

# Expose ports
EXPOSE 8080
EXPOSE 8443

# Prepare and set the entry point
RUN echo '#!/bin/sh' > start.sh \
    && echo "exec java $JVM_ARGS -cp /app:/app/lib/* -Djava.security.egd=file:/dev/./urandom \\" >> start.sh \
    && echo "-Dspring.profiles.active=$SPRING_PROFILE \\" >> start.sh \
    && cat $APP_DIR/META-INF/MANIFEST.MF | grep 'Start-Class: ' | cut -d' ' -f2 | tr -d '\r\n' >> start.sh \
    && echo "" >> start.sh \
    && cat start.sh \
    && chmod +x start.sh
ENTRYPOINT [ "/app/start.sh" ]
