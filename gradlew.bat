@rem
@rem Gradle start up script for Windows
@rem

@if "%DEBUG%" == "" @echo off
@setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
set BASE64_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar.base64

if not exist "%CLASSPATH%" (
    if exist "%BASE64_JAR%" (
        powershell -NoProfile -Command "Set-StrictMode -Version Latest; $jarPath = '%CLASSPATH%'; $base64Path = '%BASE64_JAR%'; $dir = Split-Path -Parent $jarPath; if (-not (Test-Path -LiteralPath $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }; $bytes = [Convert]::FromBase64String([IO.File]::ReadAllText($base64Path)); [IO.File]::WriteAllBytes($jarPath, $bytes)" || goto fail
    ) else (
        echo Gradle wrapper JAR is missing. Please regenerate it with 'gradle wrapper'.
        goto fail
    )
)

if not exist "%CLASSPATH%" (
    echo Gradle wrapper JAR is missing and could not be restored.
    goto fail
)

set JAVA_EXE=java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

for %%i in (java.exe) do set JAVA_EXE=%%~$PATH:i
if "%JAVA_EXE%" == "" (
    echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    goto fail
)
goto execute

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto execute

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto fail

:execute
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:fail
exit /b 1
