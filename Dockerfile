# Stage 1: Build WAR application with Maven and Eclipse Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy project source and package the WAR artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run application with Apache Tomcat 10.1 (Jakarta EE 10) & Eclipse Temurin JDK 21
FROM tomcat:10.1-jdk21-temurin
WORKDIR /usr/local/tomcat

# Remove default pre-installed Tomcat webapps
RUN rm -rf webapps/*

# Copy packaged WAR to ROOT.war (accessible at http://localhost:8080/)
# and WMS_war.war (for backward-compatible context path)
COPY --from=build /app/target/wms-app.war webapps/ROOT.war
COPY --from=build /app/target/wms-app.war webapps/WMS_war.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
