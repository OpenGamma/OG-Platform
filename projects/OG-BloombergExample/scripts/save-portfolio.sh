#!/bin/sh

cd `dirname $0`/..

scripts/run-tool.sh com.opengamma.integration.tool.portfolio.PortfolioSaverTool $@ -c config/toolcontext/bloombergexample-bin.properties -l com/opengamma/util/test/warn-logback.xml
