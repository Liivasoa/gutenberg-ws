FROM maven:3.9.12-eclipse-temurin-25

WORKDIR /app

EXPOSE 8080

# Use system Maven to run the application
CMD ["mvn", "spring-boot:run"]