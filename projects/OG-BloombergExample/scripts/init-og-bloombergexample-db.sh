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

if [ "`basename $0`" = "init-og-bloombergexample-db.sh" ] ; then
  cd `dirname $0`/.. #PLAT-1527
fi

CLASSPATH=config:og-bloombergexample.jar
for FILE in `ls -1 lib/*` ; do
  CLASSPATH=$CLASSPATH:$FILE
done

echo "### Creating empty database"

$JAVA  -cp "$CLASSPATH" \
  com.opengamma.util.test.DbTool \
  -jdbcUrl jdbc:hsqldb:file:install/db/hsqldb/bloombergexample-db \
  -database og-financial \
  -user "OpenGamma" \
  -password "OpenGamma" \
  -drop true \
  -create true \
  -createtables true \
  -dbscriptbasedir .

$JAVA  -cp "$CLASSPATH" \
  com.opengamma.util.test.DbTool \
  -jdbcUrl jdbc:hsqldb:file:temp/hsqldb/og-fin-user \
  -database og-financial \
  -user "OpenGamma" \
  -password "OpenGamma" \
  -drop true \
  -create true \
  -createtables true \
  -dbscriptbasedir .

echo "### Adding Bloomberg example data"

$JAVA  -cp "$CLASSPATH" \
  -Xms1024M \
  -Xmx4096M \
  -Dlogback.configurationFile=bloombergexample-logback.xml \
  com.opengamma.bloombergexample.tool.ExampleDatabasePopulater \
  -c classpath:toolcontext/bloombergexample-bin.properties

echo "### Completed"

