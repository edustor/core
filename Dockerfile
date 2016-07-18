FROM java:8-jdk

RUN apt-get update && apt-get install -y libgs-dev
ADD . /code/src

WORKDIR /code/src
RUN ./gradlew build

WORKDIR /code
RUN cp src/backend/build/dist/edustor.jar .

CMD java -jar edustor.jar