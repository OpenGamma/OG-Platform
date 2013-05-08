@echo off
	setlocal
	OPTS=-elevate
	if "%PROJECT%" == "" goto noprj
	OPTS=%OPTS% -p%PROJECT%.jar
:noprj
	"%~dp0\..\bin\runtool.exe" %OPTS% %*
