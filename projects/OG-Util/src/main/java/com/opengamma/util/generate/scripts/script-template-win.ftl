@ECHO OFF


set PROJECT=${project}
set PROJECTJAR=%PROJECT%.jar

CALL %~dp0\run-tool.bat ${className} %* -l com/opengamma/util/warn-logback.xml
