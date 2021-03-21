#!/bin/bash

PID_FILE=process

kill `cat $PID_FILE.pid`
rm -f $PID_FILE.pid
