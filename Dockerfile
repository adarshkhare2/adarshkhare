# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/portfolio-1.0.0.jar app.jar
# Seed initial content (remove these two lines if you mount a persistent volume).
COPY data ./data
COPY uploads ./uploads
ENV PORT=8080
ENV PORTFOLIO_UPLOAD_DIR=./uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
