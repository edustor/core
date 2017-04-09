FROM quay.io/edustor/core-base

ADD build/dist/edustor.jar .

HEALTHCHECK CMD curl -f http://localhost:8080/version
CMD java -jar edustor.jar