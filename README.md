TV Scheduler Pro JC Build
Copyright John Calvi 2010-2025.
Original Code Copyright (c) 2009 Blue Bit Solutions (www.bluebit.com.au)

Version 298+ Requires Java 25 or higher.
  1. Minimum JAVA Version 25.


Version 296+ Requires Java 21 or higher.
  1. Minimum JAVA Version 21.
  2. Resets the "email notifications > security" setting. Be sure to re-select the appropriate protocol if you had STARTTLS or SSL selected previously.
  3. Has a default filename type option. If upgrading from earlier version you need to add the "default" filenametype if you want to use it and assign it to auto-adds as desired.

Version 285+ Moves Data from the install directory to the ProgramData Directory
BACKUP BEFORE INSTALLING JUST IN CASE!!!

Version 282+ Requires Java 11 or higher.
Fully compatible with OpenJava, no need to run a java installer (but you can if you want).

Please use the following instructions to get a working installed system.

Versions 285+

64-Bit Version
1. Download "https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot#x64_win"

If you have downloaded the installable Java version then
    2. Install the Java Version, follow the prompts.

OR if you have downloaded the zip version and want to install to a folder of your choice...
    2. Extract it to a folder of your choice,
      a. eg. C:\Program Files\Java\jre
      b. Add the bin folder to your windows path, ie. edit environment variables and add C:\Program Files\Java\jre\bin

OR if you have downloaded the zip version and you prefer it to be all part of TVSP...
    2. Extract it to C:\Program Files\TV Scheduler Pro\jre, (or wherever you have installed TVSP, the installer will edit the wrapper.conf automatically).

3. Install TV Scheduler Pro, it should install and startup.

Note: DO NOT have both a 32 and 64 bit version of JAVA on your system using the environment path method.
  Even if the 32bit one is not in the path, if the folders have the same names and the 64bit version is in the path the wrapper gets confused.
  If you must have both for other reasons ensure the 64bit version is first in the path and the 32bit version has a different folder name
  e.g. 64bit -> C:\Program Files\Java\jdk-11.0.2+9-jre
       32bit -> C:\Program Files (x86)\Java32\jdk-11.0.2+9-jre
  The wrapper does not differentiate the (x86) part properly.
  OR, install the java version required into the TVSP folder, eg "C:\Program Files\TV Scheduler Pro\jre"
  Another solution is to edit the wrapper.conf file and point it directly to the correct installation.
    The disadvantage with this is updating java. This can be eased by making the folder name more generic
    e.g. C:\Program Files\Java\jre  OR...
         C:\TV Scheduler Pro\jre
    That way you can copy new Java versions into the same folder (Don't forget to stop the TVSP service first)

32-Bit Version on a 32bit OS
1. Download "https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot#x32_win"
2. Extract it to a folder of your choice,
  a. eg. C:\Program Files\Java\jre
  b. Add the bin folder to your windows path, ie. edit environment variables and add C:\Program Files\Java\jre\bin
OR if you prefer it to be all part of TVSP...
2. Extract it to C:\Program Files\TV Scheduler Pro\jre, installer will edit the wrapper.conf automatically.
3. Install TV Scheduler Pro, it should install and startup.

Note: I do not recommend installing 32bit TVSP on a 64bit OS.
The 64bit version works perfectly fine and there will be less issues with supported Java etc in the future.



Versions 282-284
64-Bit Version
1. Download "https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot#x64_win"
2. Extract it to a folder of your choice,
  a. recommended is C:\Program Files\Java\jre
  b. Add the bin folder to your windows path, ie. edit environment variables and add C:\Program Files\Java\jre\bin
OR if you prefer it to be all part of TVSP...
2. Extract it to C:\TV Scheduler Pro\jre
  a. try to install and when it errors on startup
  b. edit wrapper.conf,
  c. comment out "wrapper.java.command=java" to "#wrapper.java.command=java"
  d. uncomment "#wrapper.java.command=jre\bin\java" to wrapper.java.command=jre\bin\java
3. Install TV Scheduler Pro, it should install and startup.
Note: DO NOT have both a 32 and 64 bit version of JAVA on your system.
  Even if the 32bit one is not in the path, if the folders have the same names and the 64bit version is in the path the wrapper gets confused.
  If you must have both for other reasons ensure the 64bit version is first in the path and the 32bit version has a different folder name
  e.g. 64bit -> C:\Program Files\Java\jdk-11.0.2+9-jre
       32bit -> C:\Program Files (x86)\Java32\jdk-11.0.2+9-jre
  The wrapper does not differentiate the (x86) part properly.
  Another solution is to edit the wrapper.conf file and point it directly to the correct installation.
    The disadvantage with this is updating java. This can be eased by making the folder name more generic
    e.g. C:\Program Files\Java\jre  OR...
         C:\TV Scheduler Pro\jre
    That way you can copy new Java versions into the same folder (Don't forget to stop the TVSP service first)


32-Bit Version (on 32bit OS)
1. Download "https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot#x32_win"
2. Extract it to a folder of your choice,
  a. recommended is C:\Program Files\Java\jdk-11.0.2+9-jre
  b. Add the bin folder to your windows path, ie. edit environment variables and add C:\Program Files\Java\jdk-11.0.2+9-jre\bin
OR if you prefer it to be all part of TVSP...
2. Extract it to C:\TV Scheduler Pro\jre
  a. try to install and when it errors on startup
  b. edit wrapper.conf,
  c. comment out "wrapper.java.command=java" to "#wrapper.java.command=java"
  d. uncomment "#wrapper.java.command=jre\bin\java" to wrapper.java.command=jre\bin\java
3. Install TV Scheduler Pro, it should install and startup.


32-Bit Version (on 64bit OS)
1. Download "https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot#x32_win"
2. Extract it to a folder of your choice,
  a. recommended is C:\Program Files (x86)\Java\jdk-11.0.2+9-jre
  b. Add the bin folder to your windows path, ie. edit environment variables and add C:\Program Files (x86)\Java\jdk-11.0.2+9-jre\bin
OR if you prefer it to be all part of TVSP...
2. Extract it to C:\TV Scheduler Pro\jre
  a. try to install and when it errors on startup
  b. edit wrapper.conf,
  c. comment out "wrapper.java.command=java" to "#wrapper.java.command=java"
  d. uncomment "#wrapper.java.command=jre\bin\java" to wrapper.java.command=jre\bin\java
3. Install TV Scheduler Pro, it should install and startup.

