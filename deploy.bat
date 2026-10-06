@echo off
echo ========================================================
echo Deploying Real-Time Hospital Queue System to Tomcat 10.1
echo ========================================================

cd /d "%~dp0"
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed.
    exit /b %ERRORLEVEL%
)

echo.
echo Deploying WAR to Tomcat webapps...
copy /Y "target\hospital-queue.war" "C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\hospital-queue.war"

if exist "C:\Users\nkgop\eclipse-workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\hospital-queue" (
    echo Syncing updated classes and webapp to Eclipse WTP...
    xcopy /E /Y /I "target\classes\*" "C:\Users\nkgop\eclipse-workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\hospital-queue\WEB-INF\classes\" >nul
    xcopy /E /Y /I "src\main\webapp\*" "C:\Users\nkgop\eclipse-workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\hospital-queue\" >nul
)

echo.
echo Deployment completed successfully!
echo URL: http://localhost:8080/hospital-queue/
echo ========================================================
