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

"%JAVACMD%" -cp "og-bloombergexample.jar;lib\*" ^
  com.opengamma.util.test.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:data/db/hsqldb/bloombergexample-db ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true
  
"%JAVACMD%" -cp "og-bloombergexample.jar;lib\*" ^
  com.opengamma.util.test.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:data/hsqldb/og-fin-user ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true
  
echo ### Adding example data

set CLASSPATH=og-bloombergexample.jar;lib\*;config
FOR /R lib %%a IN (*.zip) DO set CLASSPATH=!CLASSPATH!;%%a

"%JAVACMD%" -cp "%CLASSPATH%" ^
  -Xms512M ^
  -Xmx1024M ^
  com.opengamma.bloombergexample.tool.ExampleDatabasePopulator ^
  -l tofile-logback.xml ^
  -c classpath:toolcontext/toolcontext-bloombergexample-bin.properties

echo ### Completed

ENDLOCAL

REM PLAT-1527
popd
