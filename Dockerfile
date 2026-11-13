# Stage 1: Build WAR application with Maven and Eclipse Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

ARG REPO_URL=https://github.com/rowtee/wms.git
ARG BRANCH=main

# Install git
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# Copy local build context
COPY . /tmp/build-context/

# Build from local context if pom.xml is present, or automatically clone public GitHub repo
RUN if [ -f "/tmp/build-context/pom.xml" ]; then \
        echo "Building from local repository context..."; \
        cp -r /tmp/build-context/* ./; \
    else \
        echo "Local context empty. Cloning directly from public repository ${REPO_URL} (${BRANCH})..."; \
        git clone --depth 1 --branch ${BRANCH} ${REPO_URL} .; \
    fi \
    && rm -rf /tmp/build-context

# Package application WAR
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
