#!/bin/sh

SCRIPT_DIR=`dirname $0`

echo Starting market data server...
nohup $SCRIPT_DIR/start-marketdata.sh > marketdata-nohup.log &
$SCRIPT_DIR/wait-marketdata.sh
if [ $? -ne 0 ]; then
  echo Error: Market data server failed to start
  exit 1
fi
echo Market data server started successfully

echo Starting engine server...
nohup $SCRIPT_DIR/start-engine.sh > engine-nohup.log &
$SCRIPT_DIR/wait-engine.sh
if [ $? -ne 0 ]; then
  echo Error: Engine server failed to start
  exit 1
fi
echo Engine server started successfully
