FROM java:8-jdk

RUN apt-get update
RUN apt-get install libgs-dev zbar-tools -y

WORKDIR /code

CMD bash