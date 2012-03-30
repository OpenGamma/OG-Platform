#!/bin/sh

if [ "`basename $0`" = "run-tool.sh" ] ; then
  cd `dirname $0`/..
fi
PROJECTJAR=og-integration.jar
if [ -f ${PROJECTJAR} ]; then
  CLASSPATH=${PROJECTJAR}:${CLASSPATH}
else
  CLASSPATH=build/${PROJECTJAR}:${CLASSPATH}
fi
CLASSPATH=config:${CLASSPATH}
for FILE in `ls -1 lib/*` ; do
  CLASSPATH=$CLASSPATH:$FILE
done

if [ ! -z "$JAVA_HOME" ]; then
  JAVA=$JAVA_HOME/bin/java
elif [ -x /opt/jdk1.6.0_25/bin/java ]; then
  JAVA=/opt/jdk1.6.0_25/bin/java
else
  # No JAVA_HOME, try to find java in the path
  JAVA=`which java 2>/dev/null`
  if [ ! -x "$JAVA" ]; then
    # No java executable in the path either
    echo "Error: Cannot find a JRE or JDK. Please set JAVA_HOME"
    exit 1
  fi
fi

MEM_OPTS="-XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC \
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing"

$JAVA $MEM_OPTS -cp $CLASSPATH $@
