@echo off
set DIR=%~dp0
set CLASSPATH=%DIR%gradle\wrapper\gradle-wrapper.jar
java -Dorg.gradle.appname=gradlew -classpath %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %*
