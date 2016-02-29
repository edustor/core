FROM java:8-jdk

RUN apt-get update && apt-get install ghostscript -y

ADD . /code/src

WORKDIR /code

RUN cd src && /code/src/gradlew build
RUN cp src/backend/build/dist/edustor.jar .
RUN rm -r src/

CMD java -jar edustor.jar