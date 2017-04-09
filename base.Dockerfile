FROM openjdk:8-jdk

RUN apt-get update && \
    apt-get install -y --no-install-recommends libgs-dev zbar-tools && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /code