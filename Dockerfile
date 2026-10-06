# Multi-stage build for Real-Time Hospital Queue System
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy project definition and source
COPY pom.xml .
COPY src ./src

# Build production WAR
RUN mvn clean package -DskipTests

# Run on Apache Tomcat 10.1 (Jakarta EE 10 compatible)
FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/hospital-queue.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
