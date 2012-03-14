#!/bin/sh

if [ "`basename $0`" = "start-marketdata.sh" ] ; then
  cd `dirname $0`/..
fi

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

JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.port=8062 -Dcom.sun.management.jmxremote.ssl=false"
MEM_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC \
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing"

$JAVA $JMX_OPTS $MEM_OPTS -jar lib/org.eclipse-jetty-jetty-start-7.0.1.v20091125.jar \
  -Djetty.port=8090 -DSTOP.PORT=8089 -DSTOP.KEY=OpenGamma \
  -Dlogback.configurationFile=marketdata-logback.xml \
  start.class=com.opengamma.production.startup.MarketDataServer \
  config/marketdata-spring.xml "path=$CLASSPATH"
