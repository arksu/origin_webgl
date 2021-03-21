#!/bin/bash

PID_FILE=process
JAR_FILE=./lib/backend.jar

rm -f ./$PID_FILE.pid
java -server \
    -cp ./lib/ \
    -jar $JAR_FILE &
echo $! > ./$PID_FILE.pid