FROM java:8-jdk

RUN apt-get update && apt-get install libgs-dev --no-install-recommends -y
ADD . /code/src

WORKDIR /code/src
RUN ./gradlew build

WORKDIR /code
RUN cp src/backend/build/dist/edustor.jar . && rm -r /root/.gradle

CMD java -jar edustor.jar