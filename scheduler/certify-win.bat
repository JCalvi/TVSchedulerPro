@echo off

for /R %%I in (win\win32\*,win\x64\*) do (
..\signtool\signtool.exe sign /a /fd sha1 /t  http://timestamp.comodoca.com /v "%%~fI"
..\signtool\signtool.exe sign /a /fd sha256 /tr http://timestamp.comodoca.com?td=sha256 /td sha256 /as /v "%%~fI"
)

pause