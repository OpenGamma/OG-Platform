#!/bin/sh

cd `dirname $0`/..

scripts/run-tool.sh com.opengamma.bbg.component.BloombergTimeSeriesUpdateTool -c config/toolcontext/bloombergexample-bin.properties -l com/opengamma/util/test/info-logback.xml
