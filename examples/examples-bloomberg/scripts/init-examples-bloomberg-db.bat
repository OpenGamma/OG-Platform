@echo off
setlocal ENABLEDELAYEDEXPANSION
REM PLAT-1527
pushd %~dp0\..

echo ### Creating populated database

CALL "%~dp0\project-utils.bat"

CALL "%~dp0\run-tool.bat" ^
  -Dlogback.configurationFile=tofile-logback.xml ^
  com.opengamma.examples.bloomberg.tool.ExampleDatabaseCreator

echo ### Completed

ENDLOCAL

REM PLAT-1527
popd
