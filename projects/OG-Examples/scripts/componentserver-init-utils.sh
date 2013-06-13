load_default_config() {
  # Read generic opengamma settings
  [ -f /etc/sysconfig/opengamma/defaults ] && . /etc/sysconfig/opengamma/defaults
  [ -f /etc/default/opengamma/defaults ] && . /etc/default/opengamma/defaults
  [ -f $HOME/.opengamma/defaults ] && . $HOME/.opengamma/defaults
}

load_component_config() {
  local _PROJECT _COMPONENT
  _PROJECT=$1; shift
  _COMPONENT=$1; shift

  # Read project/service specific settings
  [ -f /etc/sysconfig/opengamma/${_PROJECT}/${_COMPONENT} ] && . /etc/sysconfig/opengamma/${_PROJECT}/${_COMPONENT}
  [ -f /etc/default/opengamma/${_PROJECT}/${_COMPONENT} ] && . /etc/default/opengamma/${_PROJECT}/${_COMPONENT}
  [ -f $HOME/.opengamma/${_PROJECT}/${_COMPONENT} ] && . $HOME/.opengamma/${_PROJECT}/${_COMPONENT}

  PIDFILE=${PIDFILE:-${_COMPONENT}.pid}
  LOGFILE=${LOGFILE:-${_COMPONENT}-console.log}
  MEM_OPTS=${MEM_OPTS:--Xms4096m -Xmx4096m -XX:MaxPermSize=256M}
  GC_OPTS=${GC_OPTS:--XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing}
  EXTRA_JVM_OPTS=${EXTRA_JVM_OPTS:-""}
  LOGBACK_CONFIG=${LOGBACK_CONFIG:-engine-logback.xml}
  SHUTDOWN_WAIT=${SHUTDOWN_WAIT:-15}
}

set_commandmonitor_opts() {
  COMMAND_PORT=${COMMAND_PORT:-8079}
  COMMAND_SECRET=${COMMAND_SECRET:-""}

  if [ x${COMMAND_SECRET} != x"" ]; then
    COMMANDMONITOR_OPTS="-Dcommandmonitor.secret=$COMMAND_SECRET -Dcommandmonitor.port=$COMMAND_PORT"
  else
    COMMANDMONITOR_OPTS=""
  fi
}

start() {
  SETSID=$(which setsid 2>/dev/null)
  if [ -z "$SETSID" -o ! -x "$SETSID" ]; then
    SETSID=
  fi
  RETVAL=0
  echo -n "Starting ${COMPONENT}"
  set_java_cmd
  set_commandmonitor_opts
  if [ -z ${CONFIG} ]; then
    echo " - CONFIG variable not specified"
    RETVAL=6
    return
  fi
  exec $SETSID $JAVA_CMD $MEM_OPTS $GC_OPTS $EXTRA_JVM_OPTS $COMMANDMONITOR_OPTS \
  -Dlogback.configurationFile=$LOGBACK_CONFIG \
  -cp $CLASSPATH \
  com.opengamma.component.OpenGammaComponentServer \
  -q ${CONFIG} > ${LOGFILE} 2>&1 < /dev/null &
  echo $! >$PIDFILE
  echo
}

debug() {
  RETVAL=0
  echo -n "Starting ${COMPONENT}"
  set_java_cmd
  set_commandmonitor_opts
  if [ -z ${CONFIG} ]; then
    echo " - CONFIG variable not specified"
    RETVAL=6
    return
  fi
  echo
  $JAVA_CMD $MEM_OPTS $GC_OPTS $EXTRA_JVM_OPTS $COMMANDMONITOR_OPTS \
  -Dlogback.configurationFile=$LOGBACK_CONFIG \
  -cp $CLASSPATH \
  com.opengamma.component.OpenGammaComponentServer \
  -q ${CONFIG}
}

showconfig() {
  set_java_cmd
  set_commandmonitor_opts
  echo "CONFIG         = $CONFIG"
  echo "LOGFILE        = $LOGFILE"
  echo "JAVA_CMD       = $JAVA_CMD"
  echo "MEM_OPTS       = $MEM_OPTS"
  echo "GC_OPTS        = $GC_OPTS"
  echo "EXTRA_JVM_OPTS = $EXTRA_JVM_OPTS"
  echo "LOGBACK_CONFIG = $LOGBACK_CONFIG"
  echo "PIDFILE        = $PIDFILE"
  echo "SHUTDOWN_WAIT  = $SHUTDOWN_WAIT"
  echo "COMMAND_PORT   = $COMMAND_PORT"
  echo "COMMAND_SECRET = $COMMAND_SECRET"
}

stop() {
  local count kpid
  RETVAL=7
  echo -n "Stopping ${COMPONENT}"
  set_java_cmd
  set_commandmonitor_opts
  # If the command interface is in use try to shutdown the server nicely
  if [ x${COMMANDMONITOR_OPTS} != x"" ]; then
    $JAVA_CMD $MEM_OPTS $EXTRA_JVM_OPTS $COMMANDMONITOR_OPTS \
    -Dlogback.configurationFile=$LOGBACK_CONFIG \
    -cp $CLASSPATH \
    com.opengamma.component.OpenGammaComponentServerMonitor exit
    # Wait a bit before sending SIGINT
    sleep $SHUTDOWN_WAIT
    RETVAL=0
  fi

  if [ -f $PIDFILE ]; then
    RETVAL=0
    count=0
    read kpid < $PIDFILE
    kwait=$SHUTDOWN_WAIT
    kill -15 $kpid 2> /dev/null
    until [ $(ps -p $kpid 2> /dev/null | grep -c $kpid 2> /dev/null) -eq '0' ] || [ $count -gt $kwait ]
    do
      sleep 1
      count=$(($count+1))
    done
    if [ $count -gt $kwait ]; then
      kill -9 $kpid 2> /dev/null
    fi
    rm -f "${PIDFILE}"
  fi
  echo
}

status() {
  local count kpid
  if [ -f $PIDFILE ]; then
    read kpid < $PIDFILE
    count=$(ps -p $kpid 2> /dev/null | grep -c $kpid 2> /dev/null)
    if [ $count -gt 0 ]; then
      echo $"${BASENAME} (pid $kpid) is running..."
      RETVAL=0
    else
      echo $"${BASENAME} dead but pid file exists"
      RETVAL=1
    fi
  else
    echo $"${BASENAME} is stopped"
    RETVAL=3
  fi
}
