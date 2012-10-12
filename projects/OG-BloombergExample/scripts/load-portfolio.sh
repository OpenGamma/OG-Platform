#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.portfolio.PortfolioLoaderTool $@ -c classpath:toolcontext/toolcontext-bloombergexample-bin.properties -l com/opengamma/util/warn-logback.xml
