FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/cti-provedor-api.jar /app/app.jar
CMD ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]
