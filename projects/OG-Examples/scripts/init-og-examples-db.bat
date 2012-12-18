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

"%JAVACMD%" -cp "og-examples.jar;lib\*" ^
  -Dlogback.configurationFile=jetty-logback.xml ^
  com.opengamma.util.test.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:install/db/hsqldb/example-db ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true ^
  -dbscriptbasedir .
  
"%JAVACMD%" -cp "og-examples.jar;lib\*" ^
  -Dlogback.configurationFile=jetty-logback.xml ^
  com.opengamma.util.test.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:temp/hsqldb/og-fin-user ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true ^
  -dbscriptbasedir .
  
echo ### Adding example data

set CLASSPATH=og-examples.jar;lib\*;config
FOR /R lib %%a IN (*.zip) DO set CLASSPATH=!CLASSPATH!;%%a

"%JAVACMD%" -cp "%CLASSPATH%" ^
  -Xms512M ^
  -Xmx1024M ^
  -Dlogback.configurationFile=jetty-logback.xml ^
  com.opengamma.examples.tool.ExampleDatabasePopulator

echo ### Completed

ENDLOCAL
REM PLAT-1527
popd
