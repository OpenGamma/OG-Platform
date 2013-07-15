#!/bin/sh
SCRIPTDIR="$(dirname "$0")"

echo "### Creating populated database"

${SCRIPTDIR}/run-tool.sh --chdirtoinstallation \
  -Xms512M \
  -Xmx1024M \
  -Dlogback.configurationFile=tofile-logback.xml \
  com.opengamma.examples.bloomberg.tool.ExampleDatabaseCreator

echo "### Completed"
