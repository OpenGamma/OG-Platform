#!/bin/sh

JAVA_HOME=/opt/jdk1.6.0_20
BASE_DIR=/opt/og-bloomberg
CLASSPATH="$BASE_DIR/config:$BASE_DIR/og-bloomberg.jar"

# add lib to classpath
for file in $BASE_DIR/lib/*
do
  CLASSPATH="$CLASSPATH:$file"
done

JAVA_OPTS="-Xmx512m -Dlogback.configurationFile=$BASE_DIR/config/bbg-hdl-logback.xml -Dopengamma.platform.runmode=shareddev"

$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH  com.opengamma.bbg.loader.BloombergHistoricalLoader --update
#$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH com.opengamma.bbg.loader.MissingHistoricalDataLoader -f "PX_LAST,VOLUME" -p "CMPL,CMPN" $BASE_DIR/config/demoCurves.txt
#$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH com.opengamma.bbg.loader.MissingHistoricalDataLoader -f "PX_LAST,VOLUME" -of "OPT_IMPLIED_VOLATILITY_LAST" -s 25 $BASE_DIR/config/demoEquity.txt