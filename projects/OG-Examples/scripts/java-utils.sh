set_java_cmd() {
  if [ ! -z "$JAVA_CMD" ]; then
    if [ ! -x "$JAVA_CMD" ]; then
      echo "Error: $JAVA_CMD isn't executable "
      exit 1
    fi
  elif [ ! -z "$JAVA_HOME" ]; then
    JAVA_CMD=$JAVA_HOME/bin/java
  else
    # No JAVA_HOME, try to find java in the path
    JAVA_CMD=`which java 2>/dev/null`
    if [ ! -x "$JAVA_CMD" ]; then
      # No java executable in the path either
      echo "Error: Cannot find a JRE or JDK. Please set JAVA_HOME"
      exit 1
    fi
  fi
}

build_classpath() {
  local _PREFIX
  _PREFIX=$1
  if [ ! -z "$_PREFIX" ]; then
    _PREFIX="${_PREFIX}/"
  fi

  _CLASSPATH=""
  if [ -f ${_PREFIX}lib/classpath.jar ]; then
    _CLASSPATH="${_PREFIX}lib/classpath.jar"
  else
    for _FILE in $(find ${_PREFIX}lib -name "*.jar" -o -name "*.zip") ; do
      _CLASSPATH=${_CLASSPATH}:${_FILE}
    done
  fi
  echo "${_CLASSPATH}"
}
