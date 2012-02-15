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

$JAVA -jar lib/org.eclipse-jetty-jetty-start-7.0.1.v20091125.jar \
  -DSTOP.PORT=8079 -DSTOP.KEY=OpenGamma --stop

for i in {0..90}
do
  sleep 1
  netstat -tnlp 2>&1 | grep -q -E ":::(8080|8052)"
  if [ $? != 0 ] ; then
    echo Server stoped
    exit 0;
  fi
  echo Waiting for server to stop
done
echo Server not started and listening on port 8080
exit 1
