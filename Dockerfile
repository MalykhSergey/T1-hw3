FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY human_core_starter/pom.xml ./human_core_starter/
COPY human_core_starter/src ./human_core_starter/src
COPY byshop-prototype/pom.xml ./byshop-prototype/
COPY byshop-prototype/src ./byshop-prototype/src

WORKDIR /app/human_core_starter

RUN mvn install

WORKDIR /app/byshop-prototype

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/byshop-prototype/target/*.jar ./app.jar