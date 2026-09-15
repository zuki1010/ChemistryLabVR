# 1. Base Image Java (chọn đúng phiên bản Java bạn đang dùng, vd: OpenJDK 17 hoặc 21)
FROM eclipse-temurin:21-jdk-alpine

# 2. Thư mục làm việc trong container
WORKDIR /app

# 3. Copy file JAR vừa build từ máy thật vào container
# (Thay chemistrylabvr-0.0.1-SNAPSHOT.jar bằng tên file JAR trong thư mục target/ hoặc build/libs/)
COPY target/*.jar app.jar

# 4. Cổng ứng dụng Spring Boot chạy
EXPOSE 10104

# 5. Lệnh khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]