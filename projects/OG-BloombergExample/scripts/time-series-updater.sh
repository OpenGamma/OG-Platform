#!/bin/sh

cd `dirname $0`/..

scripts/run-tool.sh com.opengamma.bbg.tool.BloombergTimeSeriesUpdateTool -c config/toolcontext/toolcontext-bloombergexample-bin.properties -l com/opengamma/util/test/info-logback.xml
