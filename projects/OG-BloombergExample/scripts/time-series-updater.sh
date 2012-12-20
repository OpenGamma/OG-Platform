#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.marketdata.BloombergTimeSeriesUpdateTool -c classpath:toolcontext/toolcontext-bloombergexample-bin.properties -l com/opengamma/util/info-logback.xml
