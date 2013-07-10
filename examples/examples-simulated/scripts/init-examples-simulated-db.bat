@echo off
setlocal ENABLEDELAYEDEXPANSION
REM PLAT-1527
pushd %~dp0\..

IF "%JAVA_HOME%" == "" (
  ECHO Warning: JAVA_HOME is not set
  SET JAVACMD=java.exe
) ELSE (
  SET JAVACMD=!JAVA_HOME!\bin\java.exe
)

echo ### Creating empty database

"%JAVACMD%" -cp "examples-simulated.jar;lib\*" ^
  -Dlogback.configurationFile=jetty-logback.xml ^
  com.opengamma.util.db.tool.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:data/masterdb/hsqldb/example-db ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true
  
"%JAVACMD%" -cp "examples-simulated.jar;lib\*" ^
  -Dlogback.configurationFile=jetty-logback.xml ^
  com.opengamma.util.db.tool.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:data/userdb/hsqldb/og-fin-user ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true
  
echo ### Adding example data

set CLASSPATH=examples-simulated.jar;lib\*;config
FOR /R lib %%a IN (*.zip) DO set CLASSPATH=!CLASSPATH!;%%a

"%JAVACMD%" -cp "%CLASSPATH%" ^
  -Xms512M ^
  -Xmx1024M ^
  -Dlogback.configurationFile=jetty-logback.xml ^
  com.opengamma.examples.simulated.tool.ExampleDatabasePopulator

echo ### Completed

ENDLOCAL
REM PLAT-1527
popd
