FROM maven:3-jdk-8 as maven

COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY ./src ./src
RUN mvn package

# our final base image
FROM openjdk:8-jre-alpine

LABEL maintainer  "Alexander Malic <alexander.malic@maastrichtuniversity.nl>"

COPY --from=maven target/xml2rdf-1.0.0-jar-with-dependencies.jar /app/xml2rdf.jar

ENTRYPOINT ["java","-jar","/app/xml2rdf.jar"]