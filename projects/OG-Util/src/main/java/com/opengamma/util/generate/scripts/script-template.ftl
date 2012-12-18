#!/bin/sh

PROJECT=${project}
export PROJECT

`dirname $0`/run-tool.sh ${className} "$@" -l com/opengamma/util/warn-logback.xml
