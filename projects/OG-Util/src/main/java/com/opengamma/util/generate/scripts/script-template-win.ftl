set PREV_DIR=%CD%

::cd /d %~dp0

set PROJECT=${project_name}
set PROJECTJAR=%PROJECT%.jar

CALL %~dp\run-tool.bat ${className} %* -l com/opengamma/util/warn-logback.xml

::chdir /d %PREV_DIR%