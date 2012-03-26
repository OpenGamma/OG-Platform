@echo on

REM PLAT-1527
pushd %~dp0\..

if "%JAVA_HOME%" == "" echo Warning: JAVA_HOME is not set
set JAVACMD=%JAVA_HOME%\bin\java.exe

"%JAVACMD%" ^
  -Xms1024m -Xmx3072m -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC ^
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing ^
  -Dlogback.configurationFile=jetty-logback.xml ^
	-Dcommandmonitor.secret=OpenGamma ^
  -cp config;og-examples.jar;lib ^
  com.opengamma.component.OpenGammaComponentServer ^
  config\fullstack\fullstack-example-bin.properties
  
REM PLAT-1527
popd