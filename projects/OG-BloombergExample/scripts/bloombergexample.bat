@ECHO OFF

REM PLAT-1527
pushd %~dp0\..

IF NOT EXIST .\temp\hsqldb .\scripts\init-bloombergexample-db.bat

IF "%JAVA_HOME%" == "" ECHO Warning: JAVA_HOME is not set
SET JAVACMD=%JAVA_HOME%\bin\java.exe
SET CMDLINE="%JAVACMD%" ^
  -Xms1024m -Xmx3072m -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC ^
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing ^
  -Dlogback.configurationFile=bloombergexample-logback.xml ^
        -Dcommandmonitor.secret=OpenGamma ^
  -cp config;og-bloombergexample.jar;lib ^
  com.opengamma.component.OpenGammaComponentServer ^
  config\fullstack\bloombergexample-bin.properties

IF "%1"=="start" "%CMDLINE%"

IF "%1"=="debug" "%CMDLINE%"

IF "%1"=="restart" "%CMDLINE%"

IF "%1"=="reload" "%CMDLINE%"

IF "%1"=="stop" ECHO Stop not supported

IF "%1"=="status" ECHO Status not supported

IF "%1"=="" ECHO Usage: %0 "start|stop|restart|status|reload|debug"

REM PLAT-1527
popd
