#!/bin/sh

cd `dirname $0`/..

BASE_DIR=`pwd`
CLASSPATH="$BASE_DIR/config:$BASE_DIR/og-bloomberg.jar"

# add lib to classpath
for file in $BASE_DIR/lib/*
do
  CLASSPATH="$CLASSPATH:$file"
done

# if running under Cygwin, make sure CLASSPATH is a valid Windows classpath
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true;
esac

if $cygwin; then
  CLASSPATH=$(cygpath -wp $CLASSPATH)
fi

# set a default JAVA_HOME, if it's not in the environment already
if [ -z "$JAVA_HOME" ]; then
  if $cygwin; then 
    JAVA_HOME="C:/jdk1.6.0_16"
  else
    JAVA_HOME="/opt/jdk1.6.0_16"
  fi
fi

# because a port is opened for monitoring, this assumes that the server is behind a firewall 
JAVA_OPTS="-Xmx512m -XX:-UseGCOverheadLimit -Dlogback.configurationFile=$BASE_DIR/config/bbgserver-logback.xml -Dcom.sun.management.jmxremote.port=8050 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH com.opengamma.bbg.livedata.BloombergLiveDataServer