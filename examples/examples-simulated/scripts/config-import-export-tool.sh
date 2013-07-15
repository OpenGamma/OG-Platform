#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.config.ConfigImportExportTool -c classpath:toolcontext/toolcontext-examplessimulated.properties -l com/opengamma/util/info-logback.xml
