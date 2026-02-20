@echo Off
echo Stopping Onedrive
rem start %LOCALAPPDATA%\Microsoft\OneDrive\OneDrive.exe /shutdown

del /Q /S ..\*.sdf
del /Q /S ipch\*
del /Q /S release\*
del /Q /S debug\*
del /Q /S ..\device\release\*
del /Q /S ..\device\debug\*
del /Q /S ..\FileWriter\release\*
del /Q /S ..\FileWriter\debug\*
del /Q /S ..\scan\release\*
del /Q /S ..\scan\debug\*
del /Q /S ..\StreamConsumer\release\*
del /Q /S ..\StreamConsumer\debug\*
del /Q /S ..\StreamProducer\release\*
del /Q /S ..\StreamProducer\debug\*
del /Q /S ..\Tray\release\*
del /Q /S ..\Tray\debug\*
del /Q /S ..\TSMemoryShare\release\*
del /Q /S ..\TSMemoryShare\debug\*
del /Q /S ..\util\release\*
del /Q /S ..\util\debug\*

rmdir ipch
rmdir release
mkdir release

"C:\Program Files\TortoiseSVN\bin\SubWCRev.exe" "..\\.." version.ver version.h
xcopy version.h ..\device /Y
xcopy version.h ..\FileWriter\src /Y
xcopy version.h ..\scan\src /Y
xcopy version.h ..\StreamConsumer\src /Y
xcopy version.h ..\StreamProducer\src /Y
xcopy version.h ..\Tray\src /Y
xcopy version.h ..\TSMemoryShare\src /Y
xcopy version.h ..\util\src /Y

echo Building Baseclasses
"C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\IDE\devenv.exe" ..\baseclasses\baseclasses.sln /Rebuild "Release|win32" /Out release\build-base-win32.log
"C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\IDE\devenv.exe" ..\baseclasses\baseclasses.sln /Rebuild "Release|x64" /Out release\build-base-x64.log

echo Building Tools
"C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\IDE\devenv.exe" Tools.sln /Rebuild "Release|win32" /Out release\build-tools-win32.log
"C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\IDE\devenv.exe" Tools.sln /Rebuild "Release|x64" /Out release\build-tools-x64.log

echo Compile Complete - Check Logs - then Copy to Scheduler Folders?
Notepad release\build-base-win32.log
Notepad release\build-base-x64.log
Notepad release\build-tools-win32.log
Notepad release\build-tools-x64.log
pause

xcopy .\release\*.* ..\..\scheduler\win /EXCLUDE:exclude.txt /S /Y

echo Clean up build folders?
pause

del /Q /S *.sdf
del /Q /S ipch\*
del /Q /S release\*
del /Q /S debug\*
del /Q /S ..\device\release\*
del /Q /S ..\device\debug\*
del /Q /S ..\FileWriter\release\*
del /Q /S ..\FileWriter\debug\*
del /Q /S ..\scan\release\*
del /Q /S ..\scan\debug\*
del /Q /S ..\StreamConsumer\release\*
del /Q /S ..\StreamConsumer\debug\*
del /Q /S ..\StreamProducer\release\*
del /Q /S ..\StreamProducer\debug\*
del /Q /S ..\Tray\release\*
del /Q /S ..\Tray\debug\*
del /Q /S ..\TSMemoryShare\release\*
del /Q /S ..\TSMemoryShare\debug\*
del /Q /S ..\util\release\*
del /Q /S ..\util\debug\*

del /Q /S ..\baseclasses\release\win32\*
del /Q /S ..\baseclasses\release\x64\*
del /Q /S ..\baseclasses\debug\win32\*
del /Q /S ..\baseclasses\debug\x64\*
del /Q /S ..\baseclasses\release\win32\baseclasses\*
del /Q /S ..\baseclasses\release\x64\baseclasses\*
del /Q /S ..\baseclasses\debug\win32\baseclasses\*
del /Q /S ..\baseclasses\debug\x64\baseclasses\*

del /Q /S .VC\*
del /Q /S ..\baseclasses\.VC\*

echo Restarting Onedrive
rem start %LOCALAPPDATA%\Microsoft\OneDrive\OneDrive.exe /background