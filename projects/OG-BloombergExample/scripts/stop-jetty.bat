@echo on

REM PLAT-1527
pushd %~dp0\..

if "%JAVA_HOME%" == "" echo Warning: JAVA_HOME is not set
set JAVACMD=%JAVA_HOME%\bin\java.exe

"%JAVACMD%" ^
	-Dcommandmonitor.secret=OpenGamma ^
  -cp config;og-examples.jar;lib ^
  com.opengamma.component.OpenGammaComponentServerMonitor
  
REM PLAT-1527
popd