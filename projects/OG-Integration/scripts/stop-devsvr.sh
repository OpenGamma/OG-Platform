#!/bin/sh

RESULT=0

echo Stopping engine server...
`dirname $0`/stop-engine.sh
if [ $? -ne 0 ]; then
  echo Error stopping engine server
  RESULT=1
fi

echo stopping market data server...
`dirname $0`/stop-marketdata.sh
if [ $? -ne 0 ]; then
  echo Error stopping market data server
  RESULT=1
fi

exit $RESULT
