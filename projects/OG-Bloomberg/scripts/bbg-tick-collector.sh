#!/bin/sh

JAVA_HOME=/opt/jdk1.6.0_16
BASE_DIR=`pwd`
CLASSPATH="$BASE_DIR/config:$BASE_DIR/og-bloomberg.jar"

# add lib to classpath
for file in lib/*
do
  CLASSPATH="$CLASSPATH:$BASE_DIR/$file"
done

JAVA_OPTS="-Xmx512m -XX:-UseGCOverheadLimit -Dlogback.configurationFile=$BASE_DIR/config/bbgtickwriter-logback.xml"

$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH com.opengamma.bbg.replay.BloombergTicksCollectorLauncher