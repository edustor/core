FROM java:8-jdk

RUN apt-get update
RUN apt-get install libgs-dev zbar-tools -y

WORKDIR /code
ADD . /code

RUN ./gradlew build

RUN mv build/dist/edustor.jar .

CMD java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar edustor.jar