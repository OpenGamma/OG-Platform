#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.portfolio.PortfolioDeleteTool $@ -l com/opengamma/util/warn-logback.xml -c classpath:toolcontext/toolcontext-ogdev.properties
