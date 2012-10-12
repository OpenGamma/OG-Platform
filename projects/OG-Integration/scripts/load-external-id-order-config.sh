#!/bin/sh

`dirname $0`/run-tool.sh com.opengamma.integration.tool.config.ExternalIdOrderConfigDocumentTool $@ -c classpath:toolcontext/toolcontext-ogdev.properties -l com/opengamma/util/warn-logback.xml
