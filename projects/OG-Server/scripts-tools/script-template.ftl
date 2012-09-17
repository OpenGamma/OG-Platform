#!/bin/sh

cd `dirname $0`/..

TOOLCONTEXT_PROPERTIES=${r"${TOOLCONTEXT_PROPERTIES:-classpath:toolcontext/toolcontext.properties}"}

scripts/run-tool.sh ${className} "$@" -c "$TOOLCONTEXT_PROPERTIES" -l com/opengamma/util/test/warn-logback.xml

