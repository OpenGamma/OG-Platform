#!/bin/sh

cd `dirname $0`/..

scripts/run-tool.sh com.opengamma.integration.tool.timeseries.TimeSeriesLoaderTool $@ -c config/toolcontext/toolcontext-ogdev.properties -l com/opengamma/util/test/warn-logback.xml
