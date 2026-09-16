# Stage 1: Tải Maven và tự động build ra file .jar trên Server
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Tạo môi trường Java gọn nhẹ để chạy file .jar
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 10104
ENTRYPOINT ["java", "-jar", "app.jar"]