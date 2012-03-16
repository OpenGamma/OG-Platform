@echo off

if "%JAVA_HOME%" == "" echo Warning: JAVA_HOME is not set
set JAVACMD=%JAVA_HOME%\bin\java.exe

set RUN_MODE=%1
if "%RUN_MODE%" == "" set RUN_MODE=shareddev

"%JAVACMD%" -jar lib\org.eclipse-jetty-jetty-start-7.0.1.v20091125.jar ^
  -Xms1024m -Xmx3072m -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC ^
  -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing ^
  -Djetty.port=8080 ^
  -Dlogback.configurationFile=engine-logback.xml ^
  -Dopengamma.platform.runmode=%RUN_MODE% ^
  -Dopengamma.platform.marketdatasource=opengamma ^
  -Dopengamma.platform.os=win ^
  start.class=com.opengamma.integration.startup.EngineServer ^
  config\engine-spring.xml ^
  "path=config;og-integration.jar" "lib=lib"
