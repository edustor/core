FROM java:8-jdk

RUN apt-get update
RUN apt-get install libgs-dev zbar-tools -y
ADD . /code/src

WORKDIR /code/src
RUN ./gradlew build && rm -r /root/.gradle && mv build/dist/edustor.jar /code/

WORKDIR /code
CMD java -jar edustor.jar