FROM adoptopenjdk:11.0.9_11-jdk-hotspot
RUN jlink --compress=2 --no-header-files --no-man-pages \
      --add-modules java.base,java.logging,java.xml,jdk.unsupported,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
      --output /usr/lib/jvm/spring-boot-runtime

FROM debian:buster-slim
COPY --from=0 /usr/lib/jvm/spring-boot-runtime /usr/lib/jvm/spring-boot-runtime
EXPOSE 8080
WORKDIR /home/standortkarte
ARG JAR_FILE=target/standortkarte-*.jar
COPY ${JAR_FILE} /home/standortkarte/app.jar
RUN chown -R 1001:0 /home/standortkarte && \
    chmod -R g=u /home/standortkarte
USER 1001
ENTRYPOINT ["/usr/lib/jvm/spring-boot-runtime/bin/java","-XX:MaxRAMPercentage=80.0","-noverify", "-XX:TieredStopAtLevel=1","-Djava.security.egd=file:/dev/./urandom","-jar","/home/standortkarte/app.jar"]
