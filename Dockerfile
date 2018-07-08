FROM gradle:4.8.1-jdk8-alpine as builder
USER root
COPY . .
ARG apiVersion
RUN gradle --no-daemon -PapiVersion=${apiVersion} build

FROM azul/zulu-openjdk-alpine:8
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fake-student-adapter-*.jar /data/fake-student-adapter.jar
CMD ["java", "-jar", "/data/fake-student-adapter.jar"]
