@echo off
setlocal enabledelayedexpansion

REM Display the Java version to confirm
java -version

REM Check if Java 21 is installed
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
    set JAVA_VERSION=%%i
    set JAVA_VERSION=!JAVA_VERSION:"=!
)

echo Detected Java version: !JAVA_VERSION!

REM Assert that the Java version is 21
if "!JAVA_VERSION:~0,2!" NEQ "21" (
    echo Error: Java 21 is not installed or not set correctly.
    exit /b 1
) else (
    echo Java 21 is correctly installed and set.
)

:: Go to project root directory
echo Go to project root directory
cd ..\..

:: Start build the Docker image for ldca-chat
echo Start build the Docker image for ldca-chat
cd chat-api\ldca-chat
call gradlew buildImage
:: --no-daemon is used to avoid the error of JAVA 21 gradle not able to use the PATH system variable
:: and failing to find the docker command
call gradlew publishImageToLocalRegistry --no-daemon

:: Return to the root directory
cd ..\..

:: Start build the Docker image for ldca-chatroom
echo Start build the Docker image for ldca-chatroom
cd chat-api\ldca-chatroom
call gradlew buildImage
:: --no-daemon is used to avoid the error of JAVA 21 gradle not able to use the PATH system variable
:: and failing to find the docker command
call gradlew publishImageToLocalRegistry --no-daemon

:: Return to the root directory
cd ..\..

:: Build and start Docker containers
echo End build the Docker image
docker-compose up --build -d

endlocal
