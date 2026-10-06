@echo off
cd /d %~dp0
if not exist target\calculator-backend-1.0.0.jar (
  echo Packaging...
  mvn -DskipTests package
)
java -jar target\calculator-backend-1.0.0.jar
