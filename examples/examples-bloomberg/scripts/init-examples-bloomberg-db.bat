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

"%JAVACMD%" -cp "examples-bloomberg.jar;lib\*" ^
  com.opengamma.util.db.tool.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:data/masterdb/hsqldb/examplesbloomberg-db ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true
  
"%JAVACMD%" -cp "examples-bloomberg.jar;lib\*" ^
  com.opengamma.util.db.tool.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:data/userdb/hsqldb/og-fin-user ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true
  
echo ### Adding example data

set CLASSPATH=examples-bloomberg.jar;lib\*;config
FOR /R lib %%a IN (*.zip) DO set CLASSPATH=!CLASSPATH!;%%a

"%JAVACMD%" -cp "%CLASSPATH%" ^
  -Xms512M ^
  -Xmx1024M ^
  com.opengamma.examples.bloomberg.tool.ExampleDatabasePopulator ^
  -l tofile-logback.xml ^
  -c classpath:toolcontext/toolcontext-examplesbloomberg-bin.properties

echo ### Completed

ENDLOCAL

REM PLAT-1527
popd
