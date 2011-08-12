#!/bin/sh

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

CLASSPATH=config:og-examples.jar
for FILE in `ls -1 lib/*` ; do
  CLASSPATH=$CLASSPATH:$FILE
done

echo "### Creating empty database"

$JAVA  -cp "$CLASSPATH" \
  com.opengamma.util.test.DBTool \
  -jdbcUrl jdbc:hsqldb:file:install/db/hsqldb/example-db \
  -database og-financial \
  -user "" \
  -password "" \
  -drop true \
  -create true \
  -createtables true \
  -dbscriptbasedir .
  
echo "### Adding example data"

$JAVA  -cp "$CLASSPATH" \
  com.opengamma.examples.loader.DemoDatabasePopulater \
  -Xms1024M \
  -Xmx4096M \
  -Dlogback.configurationFile=jetty-logback.xml \
  -Dopengamma.platform.runmode=example \
  -Dopengamma.platform.marketdatasource=direct \
  -Dopengamma.platform.os=posix