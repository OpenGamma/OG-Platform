#!/bin/sh

for i in {0..90}
do
  sleep 1
  netstat -nlp 2>&1 | grep ":::8080"
  if [ $? == 0 ] ; then
    echo Server started and listening on port 8080
    exit 0;
  fi
  echo Waiting for server to start
done
echo Server not started and listening on port 8080
exit 1
