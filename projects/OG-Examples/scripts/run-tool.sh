#!/bin/bash

canonicalize() {
  local _TARGET _BASEDIR
  _TARGET="$0"
  readlink -f $_TARGET 2>/dev/null || (
    cd $(dirname "$_TARGET")
    _TARGET=$(basename "$_TARGET")

    while [ -L "$_TARGET" ]
    do
      _TARGET=$(readlink "$_TARGET")
      cd $(dirname "$_TARGET")
      _TARGET=$(basename "$_TARGET")
    done
    _BASEDIR=$(pwd -P)
    echo "$_BASEDIR/$_TARGET"
  )
}

BASENAME=${0##*/}
COMPONENT=${BASENAME%.sh}
BASEDIR="$(dirname "$(dirname "$(canonicalize "$0")")")"
SCRIPTDIR=${BASEDIR}/scripts

[ -f ${SCRIPTDIR}/project-utils.sh ] && . ${SCRIPTDIR}/project-utils.sh
. ${SCRIPTDIR}/java-utils.sh

[ -f /etc/sysconfig/opengamma/tools ] && . /etc/sysconfig/opengamma/tools
[ -f /etc/default/opengamma/tools ] && . /etc/default/opengamma/tools
[ -f $HOME/.opengamma/tools ] && . $HOME/.opengamma/tools

MEM_OPTS=${MEM_OPTS:--Xms512m -Xmx1024m -XX:MaxPermSize=256M}
GC_OPTS=${GC_OPTS:--XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing}

CLASSPATH=$(build_classpath ${BASEDIR})
if [ ! -z "${PROJECTJAR}" ]; then
  if [ -f ${BASEDIR}/${PROJECTJAR} ]; then
    CLASSPATH=${BASEDIR}/${PROJECTJAR}:${CLASSPATH}
  elif [ -f ${BASEDIR}/build/${PROJECTJAR} ]; then
    CLASSPATH=${BASEDIR}/build/${PROJECTJAR}:${CLASSPATH}
  fi
fi
CLASSPATH=${BASEDIR}/config:${CLASSPATH}

set_java_cmd

$JAVA_CMD $MEM_OPTS $GC_OPTS -cp $CLASSPATH "$@"
