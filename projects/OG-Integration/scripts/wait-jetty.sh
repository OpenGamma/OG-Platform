#!/bin/sh

PORT=$1

if [ -z "$PORT" ]; then
  echo Usage: $0 PORT
  exit 1
fi

for i in {0..90}
do
  sleep 1
  netstat -nlp 2>&1 | grep ":::$PORT"
  if [ $? == 0 ] ; then
    echo Server started and listening on port $PORT
    exit 0;
  fi
  echo Waiting for server to start listening on port $PORT
done
echo Server not started and listening on port $PORT
exit 1
