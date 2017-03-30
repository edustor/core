FROM openjdk:8-jdk

RUN apt-get update && \
    apt-get install -y --no-install-recommends libgs-dev zbar-tools && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /code
ADD . /code/src

RUN cd src && ./gradlew build

RUN mv ./src/build/dist/edustor.jar .

RUN rm -rf src /root/.gradle

HEALTHCHECK CMD curl -f http://localhost:8080/version
CMD java -jar edustor.jar