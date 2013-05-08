@echo off
	setlocal
	set OPTS=-elevate
	if "%PROJECT%" == "" goto noprj
	set OPTS=%OPTS% -p%PROJECT%.jar
:noprj
	"%~dp0\..\bin\runtool.exe" %OPTS% %*
