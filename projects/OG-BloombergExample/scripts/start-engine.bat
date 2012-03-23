@echo on

REM PLAT-1527
pushd %~dp0\..

if "%JAVA_HOME%" == "" echo Warning: JAVA_HOME is not set
set JAVACMD=%JAVA_HOME%\bin\java.exe

<<<<<<< Updated upstream
"%JAVACMD%" ^
||||||| merged common ancestors
set RUN_MODE=%1
if "%RUN_MODE%" == "" set RUN_MODE=example

"%JAVACMD%" -jar lib\org.eclipse-jetty-jetty-start-7.0.1.v20091125.jar ^
=======
"%JAVACMD%" -jar lib\org.eclipse-jetty-jetty-start-7.0.1.v20091125.jar ^
>>>>>>> Stashed changes
  -Xms1024m -Xmx3072m -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC ^
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing ^
  -Dlogback.configurationFile=jetty-logback.xml ^
<<<<<<< Updated upstream
	-Dcommandmonitor.secret=OpenGamma ^
  -cp config;og-examples.jar;lib ^
  com.opengamma.component.OpenGammaComponentServer ^
  config\fullstack\fullstack-example-bin.properties
||||||| merged common ancestors
  -Dopengamma.platform.runmode=%RUN_MODE% ^
  -Dopengamma.platform.marketdatasource=direct ^
  -Dopengamma.platform.os=win ^
  start.class=com.opengamma.examples.startup.ExampleServer ^
  config\engine-spring.xml ^
  "path=config;og-examples.jar" "lib=lib"
=======
  -Dcommandmonitor.secret=OpenGamma -Dcommandmonitor.port=8079 ^
  com.opengamma.component.OpenGammaComponentServer ^
  -q classpath:fullstack/fullstack-example.properties
>>>>>>> Stashed changes
  
REM PLAT-1527
popd