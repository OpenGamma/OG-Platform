#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.portfolio.PortfolioTemplateCreationTool $@ -c classpath:toolcontext/toolcontext-ogdev.properties -l com/opengamma/util/warn-logback.xml
