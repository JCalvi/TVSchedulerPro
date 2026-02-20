@echo off 
@echo Press Enter to create a slim JRE including required modules for TV Scheduler Pro and most other java apps

set JAVA_HOME=C:\Program Files\Java\x64\jdk
echo Using: %JAVA_HOME%

rem @echo Listing modules required for TV Scheduler Pro JRE
rem "%JAVA_HOME%\bin\jdeps" -s *.jar > modules.txt
rem @echo Listing modules required for Tiny Media Manager
rem "%JAVA_HOME%\bin\jdeps" -s D:\Command\TMMPortable\*.jar >> modules.txt
rem "%JAVA_HOME%\bin\jdeps" --multi-release 9 -s D:\Command\TMMPortable\lib\*.jar >> modules.txt

echo Making Slim JRE with required modules for most applications.
"%JAVA_HOME%\bin\jlink" --add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se,java.security.jgss,java.security.sasl,java.smartcardio,java.sql,java.sql.rowset,java.transaction.xa,java.xml,java.xml.crypto,jdk.accessibility,jdk.charsets,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.crypto.mscapi,jdk.dynalink,jdk.httpserver,jdk.jdwp.agent,jdk.jsobject,jdk.localedata,jdk.management,jdk.management.agent,jdk.naming.dns,jdk.naming.rmi,jdk.net,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported,jdk.xml.dom,jdk.zipfs --output jre --strip-debug --no-man-pages --no-header-files --compress=2


pause:
