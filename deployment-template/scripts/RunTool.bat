@echo off
   
rem	Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies

rem	Please see distribution for license.

	setlocal

rem	Set up the local environment, relative to this file which should be in INSTALL_DIR\platform\scripts
	set PLATFORM_SCRIPTS_PATH=%~dp0
	set PLATFORM_DIR=%PLATFORM_SCRIPTS_PATH%..
	set INSTALL_DIR=%PLATFORM_DIR%\..
	set PLATFORM_CONFIG_DIR=%PLATFORM_DIR%\config
	set SITE_CONFIG_DIR=%INSTALL_DIR%\config
	set PLATFORM_LIB_DIR=%PLATFORM_DIR%\lib
	set SITE_LIB_DIR=%INSTALL_DIR%\lib
	set SITE_SCRIPTS_DIR=%INSTALL_DIR%\scripts
	set LOG_DIR=%PLATFORM_DIR%\logs
	set DATA_DIR=%PLATFORM_DIR%\data

rem	Call user environment options (setenv.bat, if defined in SITE_SCRIPTS_DIR)
	if not exist "%SITE_SCRIPTS_DIR%\setenv.bat" goto seok
	call "%SITE_SCRIPTS_DIR%\setenv.bat"
:seok

rem	Set up the classpath
	if "%CLASSPATH%" == "" goto cpok
	set CLASSPATH=%CLASSPATH%;
:cpok
	set CLASSPATH=%CLASSPATH%%SITE_CONFIG_DIR%
rem	TODO: The line below may be wrong - does each need to be specified manually?
	set CLASSPATH=%CLASSPATH%;%SITE_LIB_DIR%\*
	set CLASSPATH=%CLASSPATH%;%PLATFORM_CONFIG_DIR%
rem	TODO: The line below may be wrong - does each need to be specified manually?
	set CLASSPATH=%CLASSPATH%;%PLATFORM_LIB_DIR%\*

rem	Set up the working directory
	cd /D "%LOG_DIR%"

rem	Set up temporary folders
	Set TMP=%DATA_DIR%
	Set TEMP=%DATA_DIR%

rem	Set up JAVA_CMD - use if set, otherwise check for JRE_HOME, JAVA_HOME, or path
	if not "%JAVA_CMD%" == "" goto tstjc
	if "%JRE_HOME%" == "" goto nojre
	set JAVA_CMD=%JRE_HOME%\bin\java
	goto tstjc
:nojre
	if "%JAVA_HOME%" == "" goto nojdk
	set JAVA_CMD=%JAVA_HOME%\bin\java
	goto tstjc
:nojdk
	where /Q java
	if errorlevel 1 goto nojav
	set JAVA_CMD=java
	goto jcok
:nojav
	echo Error: Cannot find a Java runtime environment.
	exit /B 1805
:tstjc
	if exist "%JAVA_CMD%" goto jcok
	if exist "%JAVA_CMD%.exe" goto jcok
	if exist "%JAVA_CMD%.com" goto jcok
	if exist "%JAVA_CMD%.bat" goto jcok
	echo Error: Can't find Java executable - %JAVA_CMD%
	exit /B 3
:jcok

Rem	Set up memory and GC opts
	if not "%MEM_OPTS%" == "" goto moset
	set MEM_OPTS=-Xms512m -Xmx1024m -XX:PermSize=128M -XX:MaxPermSize=256M
:moset
	if not "%GC_OPTS%" == "" goto goset
	set GC_OPTS=-XX:+UseParallelGC -XX:+UseParallelOldGC
:goset
	if "%JAVA_OPTS%" == "" goto jook
	set "JAVA_OPTS=%JAVA_OPTS% "
:jook
	set JAVA_OPTS=%JAVA_OPTS%%MEM_OPTS%
	set JAVA_OPTS=%JAVA_OPTS% %GC_OPTS%

Rem	Run the tool
Rem	set
	"%JAVA_CMD%" %JAVA_OPTS% %*
