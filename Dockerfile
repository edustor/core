FROM openjdk:8-jdk

RUN apt-get update
RUN apt-get install -y --no-install-recommends libgs-dev zbar-tools

WORKDIR /code
ADD . /code

RUN ./gradlew build

RUN mv build/dist/edustor.jar .

HEALTHCHECK CMD curl -f http://localhost:8080/version
CMD java -jar edustor.jar