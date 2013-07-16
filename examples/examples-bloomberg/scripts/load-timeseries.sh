#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.marketdata.TimeSeriesLoaderTool $@ -c config/toolcontext/toolcontext-examplesbloomberg-bin.properties -l com/opengamma/util/warn-logback.xml
