#!/bin/sh

if [ "`basename $0`" = "batch-runner.sh" ] ; then
  cd `dirname $0`/..
fi

if [ ! -z "$1" ]; then
  RUN_MODE="$1"
else
  RUN_MODE=shareddev
fi

CLASSPATH=config:og-integration.jar
for FILE in `ls -1 lib/*` ; do
  CLASSPATH=$CLASSPATH:$FILE
done

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


MEM_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=128M -XX:+UseConcMarkSweepGC \
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing"

DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=n,address=10.0.2.76:5005"

$JAVA $MEM_OPTS -cp $CLASSPATH com.opengamma.batch.BatchJobRunner $@

