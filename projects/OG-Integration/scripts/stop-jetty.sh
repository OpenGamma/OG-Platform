#!/bin/sh

if [ "`basename $0`" = "stop-jetty.sh" ] ; then
  cd `dirname $0`/..
fi

if [ ! -z "$JAVA_HOME" ]; then
  JAVA=$JAVA_HOME/bin/java
elif [ -x /opt/jdk1.6.0_16/bin/java ]; then
  JAVA=/opt/jdk1.6.0_16/bin/java
else
  # No JAVA_HOME, try to find java in the path
  JAVA=`which java 2>/dev/null`
  if [ ! -x "$JAVA" ]; then
    # No java executable in the path either
    echo "Error: Cannot find a JRE or JDK. Please set JAVA_HOME"
    exit 1
  fi 
fi

HTTP_PORT=$1
JMX_PORT=$2
STOP_PORT=$3

if [ -z "$HTTP_PORT" -o -z "$JMX_PORT" -o -z "$STOP_PORT" ]; then
  echo "Usage: $0 HTTP_PORT JMX_PORT STOP_PORT"
  exit 1
fi

PID=`fuser $HTTP_PORT/tcp 2>/dev/null` || PID=`fuser $JMX_PORT/tcp 2>/dev/null`

$JAVA -jar lib/org.eclipse-jetty-jetty-start-7.0.1.v20091125.jar \
  -DSTOP.PORT=$STOP_PORT -DSTOP.KEY=OpenGamma --stop

for i in {0..90}
do
  sleep 1
  netstat -tnlp 2>&1 | grep -q -E ":::($HTTP_PORT|$JMX_PORT)"
  if [ $? != 0 ] ; then
  	echo Server not listening
  	if [[ "$PID" -eq "" ]] ; then
    	echo Did not get PID, so not waiting for termination
    	exit 0;
    fi
    
  	ps $PID >/dev/null 2>&1
  	if [ $? -ne 0 ] ; then
    	echo Server stopped
    	exit 0;
    fi
  fi
  echo Waiting for server to stop
done
echo Server not stopped
exit 1
