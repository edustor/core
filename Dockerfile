FROM java:8-jdk

RUN apt-get update
RUN apt-get install libgs-dev --no-install-recommends -y
ADD . /code/src

WORKDIR /code/src
RUN ./gradlew build && rm -r /root/.gradle

WORKDIR /code
RUN cp src/backend/build/dist/edustor.jar .

CMD java -jar edustor.jar