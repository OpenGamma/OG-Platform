@ECHO OFF

set PROJECT=${project}

CALL "%~dp0\run-tool.bat" ${className} %* -l com/opengamma/util/warn-logback.xml
