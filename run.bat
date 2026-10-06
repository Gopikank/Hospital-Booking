@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
set "CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 10.1"

echo Starting Apache Tomcat 10.1 on port 8080...
"%CATALINA_HOME%\bin\catalina.bat" run
