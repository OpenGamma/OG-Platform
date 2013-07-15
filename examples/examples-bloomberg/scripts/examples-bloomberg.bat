@ECHO OFF

REM PLAT-1527
PUSHD %~dp0\..

set BASEDIR=%cd%
set SCRIPTDIR=%BASEDIR%\scripts
set LOGBACK_CONFIG=jetty-logback.xml
set CONFIG=config\fullstack\fullstack-examplesbloomberg-bin.properties
set MEM_OPTS=-Xms768m -Xmx1280m -XX:MaxPermSize=256M
set GC_OPTS=-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing

SETLOCAL EnableDelayedExpansion

IF NOT EXIST %BASEDIR%\data\masterdb\hsqldb\examplesbloomberg-db.properties goto :nodb 

IF "%1"=="start" goto :start
IF "%1"=="debug" goto :start
IF "%1"=="restart" goto :start
IF "%1"=="reload" goto :start
IF "%1"=="stop" ECHO Stop not supported
IF "%1"=="status" ECHO Status not supported
IF "%1"=="" ECHO Usage: %0 start^|stop^|restart^|status^|reload^|debug
GOTO :exit

:nodb
ECHO The %PROJECT% database could not be found.
ECHO Please run %SCRIPTDIR%\init-%PROJECT%-db.bat to create and populate the database.
ECHO Exiting immediately...
GOTO :exit

:start
CALL "%~dp0\project-utils.bat"

CALL "%~dp0\run-tool.bat" ^
  -Dlogback.configurationFile=%LOGBACK_CONFIG% ^
  com.opengamma.component.OpenGammaComponentServer ^
  %CONFIG%
GOTO :exit

:exit
ENDLOCAL
REM PLAT-1527
POPD
EXIT /B
