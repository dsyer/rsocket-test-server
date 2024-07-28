FROM eclipse-temurin:21-jre-alpine
#ARG RSOCKET_MOCK_SPEC_PATH
RUN mkdir "catalog"
#COPY $RSOCKET_MOCK_SPEC_PATH catalog
COPY target/fake-rsocket-server-0.0.1-SNAPSHOT-exec.jar fake-rsocket-server-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar", "-Dspring.rsocket.server.port=7000","-Duse.filesystem.resources=true", "/fake-rsocket-server-0.0.1-SNAPSHOT.jar"]
