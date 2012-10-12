#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.portfolio.PortfolioLoaderTool $@ -c classpath:toolcontext/toolcontext-ogdev.properties -l com/opengamma/util/warn-logback.xml
