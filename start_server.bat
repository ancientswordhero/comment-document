@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
cd c:\Users\36295\Desktop\shuhai\library-server
call .\mvnw spring-boot:run