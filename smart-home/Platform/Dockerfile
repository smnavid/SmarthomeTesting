# Get the Gradle image so we can build JAR file first
FROM gradle:4.7.0-jdk8-alpine AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew shadowJar

# Get the Java version 8 image
FROM java:8

# Fetch the archived repository. Unfortunately, jessie isn't there anymore (repo).
RUN echo "deb [check-valid-until=no] http://cdn-fastly.deb.debian.org/debian jessie main" > /etc/apt/sources.list.d/jessie.list
RUN echo "deb [check-valid-until=no] http://archive.debian.org/debian jessie-backports main" > /etc/apt/sources.list.d/jessie-backports.list
RUN sed -i '/deb http:\/\/deb.debian.org\/debian jessie-updates main/d' /etc/apt/sources.list
RUN apt-get -o Acquire::Check-Valid-Until=false update

# Install JavaFX
RUN apt-get install -y openjfx

WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/tartan-1.0-SNAPSHOT.jar /app
COPY config.docker.yml /app
EXPOSE 8080
EXPOSE 8081

CMD ["java", "-jar","tartan-1.0-SNAPSHOT.jar","server","config.docker.yml"]

