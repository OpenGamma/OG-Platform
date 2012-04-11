#!/bin/sh

cd `dirname $0`/..

scripts/run-tool.sh com.opengamma.integration.tool.portfolio.PortfolioDeleteTool $@ -l com/opengamma/util/test/warn-logback.xml -c classpath:toolcontext/toolcontext-ogdev.properties
