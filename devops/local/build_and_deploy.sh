#!/bin/bash
set -e

echo "go to project root directory"
cd ../..

echo "erase kafka directory"
rm -rf kafka/data/*

echo "start build the Docker image"
cd ./chat-api/ldca-chat
./gradlew buildImage
# --no-daemon is used to avoid the error of JAVA 21 gradle not able to use the PATH system variable
# and failing to find the docker command
./gradlew publishImageToLocalRegistry --no-daemon

cd ../..

cd ./chat-api/ldca-chatroom
./gradlew buildImage
# --no-daemon is used to avoid the error of JAVA 21 gradle not able to use the PATH system variable
# and failing to find the docker command
./gradlew publishImageToLocalRegistry --no-daemon

echo "end build the Docker image"

cd ../..

docker-compose up --build --force-recreate -d

