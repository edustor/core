FROM ubuntu:15.10

RUN apt-get update && apt-get install -y libgs-dev curl openjdk-8-jdk
RUN update-ca-certificates -f
#RUN curl -sL https://deb.nodesource.com/setup_5.x | bash -
#RUN apt-get install -y nodejs

ADD . /code/src

WORKDIR /code/src
#RUN cd frontend && npm install && npm run build
#RUN cp -R frontend/build/ backend/src/main/resources/static
RUN ./gradlew build

WORKDIR /code
RUN cp src/backend/build/dist/edustor.jar .
#RUN rm -r src/

CMD java -jar edustor.jar