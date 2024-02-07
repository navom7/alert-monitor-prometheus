#FROM openjdk:17
#ARG JAR_FILE=build/libs/*.jar
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
#

# Use the official OpenJDK image as the base image
FROM openjdk:17

# Set a working directory inside the image
WORKDIR /app

# Copy the built JAR file(s) into the /app directory inside the image
COPY build/libs/alert-monitor-prometheus-0.0.1-SNAPSHOT.jar ./

# Identify the main JAR (assuming there's only one Spring Boot app JAR) and set it as the ENTRYPOINT
ENTRYPOINT ["sh", "-c", "java -jar $(ls *.jar | head -n 1)"]
