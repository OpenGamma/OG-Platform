#!/bin/bash

BASENAME=${0##*/}                                                                                                               
COMPONENT=${BASENAME%.sh}                                                                                                       
# yuck, because readlink -f not available on OS X.
cd $(dirname $0)/..

BASEDIR=$(pwd) 

SCRIPTDIR=${BASEDIR}/scripts
PROJECT=og-bloombergexample
PROJECTJAR=${PROJECT}.jar

cd "${BASEDIR}" || exit 1

if [ ! -f ${BASEDIR}/install/db/hsqldb/bloombergexample-db.properties ]; then
  echo The ${PROJECT} database could not be found.
  echo Please run ${SCRIPTDIR}/init-${PROJECT}-db.sh to create and populate the database.
  echo Exiting immediately...
  exit
fi

. ${SCRIPTDIR}/componentserver-init-utils.sh

# Read default configs
load_default_config

# Component specific default configs
CONFIG=classpath:fullstack/bloombergexample-bin.properties
LOGBACK_CONFIG=jetty-logback.xml

# User customizations
load_component_config ${PROJECT} ${COMPONENT}

CLASSPATH=$(build_classpath)
if [ -f ${PROJECTJAR} ]; then
  CLASSPATH=${PROJECTJAR}:${CLASSPATH}
else
  CLASSPATH=build/${PROJECTJAR}:${CLASSPATH}
fi
CLASSPATH=config:${CLASSPATH}

RETVAL=0
case "$1" in
  start)
    start
    ;;
  debug)
    debug
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  restart|reload)
    stop
    start
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|reload|debug}"
esac

exit ${RETVAL}
