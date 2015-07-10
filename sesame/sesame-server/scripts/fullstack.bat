@ECHO OFF

PUSHD %~dp0\..

set BASEDIR=%cd%
set SCRIPTDIR=%BASEDIR%\scripts
set LOGBACK_CONFIG=fullstack\fullstack-logback.xml
set CONFIG=..\config\fullstack\fullstack.properties
set MEM_OPTS=-Xms768m -Xmx3726m -XX:MaxPermSize=256M
set GC_OPTS=-XX:+UseConcMarkSweepGC

SETLOCAL EnableDelayedExpansion

IF "%1"=="start" goto :start
IF "%1"=="debug" goto :start
IF "%1"=="restart" goto :start
IF "%1"=="reload" goto :start
IF "%1"=="stop" ECHO Stop not supported
IF "%1"=="status" ECHO Status not supported
IF "%1"=="" ECHO Usage: %0 start^|stop^|restart^|status^|reload^|debug
GOTO :exit
:start

CALL "%~dp0\RunTool.bat" ^
  -Dlogback.configurationFile=%LOGBACK_CONFIG% ^
  com.opengamma.component.OpenGammaComponentServer ^
  %CONFIG%
GOTO :exit

:exit
ENDLOCAL

POPD
EXIT /B