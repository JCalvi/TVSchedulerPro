@echo off

REM Use SVN to set next version.
"C:\Program Files\TortoiseSVN\bin\SubWCRev.exe" "..\\" version.ver version.txt -q
set /p Version=<version.txt
REM substring=string:~start,length
set Version=%Version:~8,8%

echo Building: %Version%
@echo.

set JAVA_HOME=C:\Program Files\Java\x64\jdk
echo Using: %JAVA_HOME%

echo !define version "%Version%" > version.nsi
copy changelog.txt ..\compiled\readme
If Not Exist .\bin md .\bin

@echo.
echo Cleaning past compilation files
if exist TVSchedulerPro.jar del TVSchedulerPro.jar
if exist bin\*.class del bin\*.class

@echo.
echo Compiling java including classes

rem "%JAVA_HOME%\bin\javac" -Xlint:deprecation,cast -h . -d bin -cp .;jakarta.activation-2.0.1.jar;jakarta.mail-2.0.3.jar java\*.java
"%JAVA_HOME%\bin\javac" -Xlint:all -h . -d bin -cp .;jakarta.activation-2.0.1.jar;jakarta.mail-2.0.3.jar java\*.java

@echo.
@echo Creating Manifest File matching classes included in compile
@echo Main-Class: Scheduler > MANIFEST.MF
@echo Class-Path: . jakarta.activation-2.0.1.jar jakarta.mail-2.0.3.jar >> MANIFEST.MF
@echo Enable-Native-Access: ALL-UNNAMED >> MANIFEST.MF

@echo.
@echo Creating Jar file
"%JAVA_HOME%\bin\jar" cfm TVSchedulerPro.jar MANIFEST.MF -C bin .

@echo.
@echo Cleaning up compilation files
if exist bin\*.class del bin\*.class
if exist java\*.class del java\*.class

@echo.
@echo.
@echo Compile Complete,   Create Zipfile?
pause:

if exist TVSchedulerPro*.zip del TVSchedulerPro*.zip

c:\command\zip -r TVSchedulerPro-%Version%.zip ./*.* -x */*.svn/* /bin/* /classes/* /java/* /data/authentication.prop *.bak *.h *.prop *.default *.mf make.bat svn*.*

@echo.
@echo.
@echo  Zip Complete?


pause:
