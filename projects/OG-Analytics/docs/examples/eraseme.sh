read -p "You must have set ANALYTICS for this to work. Press Ctrl-C and do this if you have not already. Else any key"
export JYTHONPATH=$ANALYTICS/lib/classpath.jar:$ANALYTICS/build/classes:$ANALYTICS/build/classes:.$ANALYTICS/lib/jar/it.unimi.dsi/fastutil/fastutil-5.1.5.jar
export JYTHONPATH=$JYTHONPATH:$ANALYTICS/tests/classes
export CLASSPATH=$JYTHONPATH
echo "Did this thing work? What is CLASSPATH?"
echo $CLASSPATH
