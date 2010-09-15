@echo off
if "%1" == "" goto error:
xcopy *.* "%1" /S /I /Y
GOTO:EOF

:error
echo "No destination file"
exit /B 1
