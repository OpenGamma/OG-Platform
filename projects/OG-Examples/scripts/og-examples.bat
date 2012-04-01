@ECHO OFF

REM PLAT-1527
PUSHD %~dp0\..

IF NOT EXIST .\temp\hsqldb echo The HSQL database directory is not present. Please run scripts\init-examples-db.bat to create and populate the database.

IF "%JAVA_HOME%" == "" ECHO Warning: JAVA_HOME is not set
SET JAVACMD=%JAVA_HOME%\bin\java.exe
SET CMDLINE=IF "%1"=="start" goto :start

IF "%1"=="debug" goto :start

IF "%1"=="restart" goto :start

IF "%1"=="reload" goto :start

IF "%1"=="stop" ECHO Stop not supported

IF "%1"=="status" ECHO Status not supported

IF "%1"=="" ECHO Usage: %0 start|stop|restart|status|reload|debug

REM PLAT-1527
POPD

EXIT


:start
"%JAVACMD%" ^
  -Xms1024m -Xmx3072m -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC ^
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing ^
  -Dlogback.configurationFile=examples-logback.xml ^
        -Dcommandmonitor.secret=OpenGamma ^
  -cp config;og-examples.jar;lib ^
  com.opengamma.component.OpenGammaComponentServer ^
  config\fullstack\example-bin.properties


