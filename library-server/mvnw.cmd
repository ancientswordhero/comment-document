@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

if not defined JAVA_HOME (
    for /f "tokens=3" %%v in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr "java.home"') do set "JAVA_HOME=%%v"
)
if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME could not be determined. Please set JAVA_HOME.
    exit /b 1
)

set "MAVEN_DIST_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin"

REM Find the Maven home directory
set "MAVEN_HOME="
for /d %%d in ("%MAVEN_DIST_DIR%\*") do (
    if exist "%%d\apache-maven-3.9.6\bin\mvn.cmd" (
        set "MAVEN_HOME=%%d\apache-maven-3.9.6"
    )
)

if defined MAVEN_HOME goto :run_maven

echo Maven distribution not found. Attempting to download...
set "MAVEN_ZIP=%USERPROFILE%\.m2\wrapper\maven-dist.zip"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"

mkdir "%MAVEN_DIST_DIR%" 2>nul
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'}"
powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_DIST_DIR%' -Force"
del "%MAVEN_ZIP%" 2>nul
set "MAVEN_HOME=%MAVEN_DIST_DIR%\apache-maven-3.9.6"

:run_maven
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo ERROR: Maven not found at %MAVEN_HOME%
    exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" -Dmaven.multiModuleProjectDirectory="%PROJECT_DIR%" %*
endlocal
