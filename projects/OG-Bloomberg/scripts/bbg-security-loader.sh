#!/bin/sh

JAVA=`which java`
BASE_DIR=../
CLASSPATH="$BASE_DIR/config:$BASE_DIR/build/og-bloomberg.jar"

# add lib to classpath
for file in lib/*
do
  CLASSPATH="$CLASSPATH:$BASE_DIR/$file"
done

JAVA_OPTS="-Xmx512m -Dlogback.configurationFile=$BASE_DIR/config/bbg-hdl-logback.xml -Dopengamma.platform.runmode=shareddev"

ARGS=$*

$JAVA $JAVA_OPTS -classpath $CLASSPATH com.opengamma.bbg.loader.BloombergSecurityFileLoader $ARGS
