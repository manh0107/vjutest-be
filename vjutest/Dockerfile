FROM maven:3.9.8-eclipse-temurin-21 AS build-stage
WORKDIR /vjutest
COPY pom.xml .
RUN mvn dependency:go-offline

COPY ./src ./src
RUN mvn clean install -Dmaven.test.skip=true

# Stage 2: Production Stage
FROM eclipse-temurin:21-jdk AS production-stage
WORKDIR /vjutest

# Copy file JAR từ build-stage vào production container
COPY --from=build-stage /vjutest/target/vjutest-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]