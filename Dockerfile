FROM java:8-jdk

RUN apt-get update
RUN apt-get install libgs-dev --no-install-recommends -y
ADD . /code/src

WORKDIR /code/src
RUN ./gradlew build && rm -r /root/.gradle && mv backend/build/dist/edustor.jar /code/

WORKDIR /code
CMD java -jar edustor.jar