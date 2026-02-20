@echo off
echo TV Scheduler Pro Update Script by J. Calvi.
echo Shuts down existing service, unzips update and restarts.

net stop TVSCHPRO
taskkill /f /im tray.exe
unzip -o "TVSchedulerPro*.zip"
start "" "C:\Program Files\TV Scheduler Pro\win\Tray.exe"
net start TVSCHPRO
