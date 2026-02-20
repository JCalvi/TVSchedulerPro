@echo off

rem change this to your JDK location
set JAVA_HOME=C:\Program Files\Java\x64\jdk
echo Using: %JAVA_HOME%

@echo Listing modules required for TV Scheduler Pro JRE
"%JAVA_HOME%\bin\jdeps" -s *.jar > modules.txt

@echo Press Enter to create JRE with required modules for TV Scheduler Pro
pause:

"%JAVA_HOME%\bin\jlink" --add-modules java.base,java.desktop,java.logging,java.security.sasl,java.xml --output jre --strip-debug --no-man-pages --no-header-files --compress=2

@echo JRE with required modules for TV Scheduler Pro Completed
pause:
