#!/bin/sh

CLASSPATH=config:london.jar
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

MEM_OPTS="-Xms512m -Xmx1024m"

mkdir -p standalone/bbgTicks

$JAVA -Dlogback.configurationFile=com/opengamma/util/test/warn-logback.xml $MEM_OPTS -cp $CLASSPATH \
  com.opengamma.integration.production.WatchListRecorder --output standalone/watchList.txt
$JAVA -Dlogback.configurationFile=com/opengamma/util/test/warn-logback.xml $MEM_OPTS -cp $CLASSPATH \
  com.opengamma.bbg.replay.BloombergTicksCollectorLauncher --duration 5
