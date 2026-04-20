@ECHO OFF
where gradle >NUL 2>NUL
IF %ERRORLEVEL% NEQ 0 (
  ECHO Gradle is not installed. Please install Gradle 8.14.3+ or add a wrapper.
  EXIT /B 1
)
CALL gradle %*
