; *******************************************************************************
; Core Installer Script - Defaults to x64 Platform
; unless called from alternate PLATFORM Selection Stub Installer Script
; Unicode True
SetCompressor /SOLID lzma
RequestExecutionLevel admin
; *******************************************************************************
; Variable Definitions
; *******************************************************************************
; If not called from PLATFORM Selection Stub - Default to x64 Build PLATFORM
!ifndef PLATFORM
  !define PLATFORM "x64"
  !define ARCHITECTURE "x64"
  !define VCREDISTURL "https://aka.ms/vs/17/release/vc_redist.x64.exe"
!endif

!define JAVAEXE "java.exe"
!define JAVAMIN "25"
!define PROJECTNS "TVSchedulerPro"
!define PROJECTWS "TV Scheduler Pro"
!define ARP "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECTNS}"
!define ProgramData "$APPDATA\${PROJECTWS}"
!define VCREDISTDES "Microsoft Visual C++ 2022"
!define VCREDISTVER "14.0"

Var wrapper_java

; *******************************************************************************
; Version Definition Include
; *******************************************************************************
!include ..\scheduler\version.nsi
; *******************************************************************************
; Name & Output Files
; *******************************************************************************
Name "${PROJECTWS} - ${version} - ${PLATFORM}"
OutFile "..\compiled\${PROJECTNS}-${version}-${PLATFORM}.exe"

; *******************************************************************************
; Interface Settings
; *******************************************************************************
; Add TVSP Icon to installer.
!include "MUI2.nsh"
!define MUI_PAGE_HEADER_TEXT "TV Scheduler Pro"
!define MUI_PAGE_HEADER_SUBTEXT "by Shaun Faulds and John Calvi"
!define MUI_ICON  ".\installer\logo.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP ".\installer\logo.bmp"
!define MUI_HEADERIMAGE_RIGHT
!define MUI_ABORTWARNING
!insertmacro MUI_LANGUAGE "English"

!include "FileFunc.nsh"
; *******************************************************************************
; The default installation directory
; *******************************************************************************
InstallDirRegKey  HKLM "${ARP}" "InstallDirectory"
InstallDir "C:\Program Files\${PROJECTWS}"

;Page directory "" "" "" ;replaced by MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_DIRECTORY
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

; *******************************************************************************
; Major Version Function to get version from Java.exe etc
; *******************************************************************************
Function MajorVersion
  Exch $R0
  Push $R1
  Push $R2
  StrLen $R1 $R0
  IntOp $R1 $R1 + 1
  loop:
    IntOp $R1 $R1 - 1
    StrCpy $R2 $R0 1 -$R1
    StrCmp $R2 "" exit2
    StrCmp $R2 "." exit1 ; Enter delimiter for version
  Goto loop
  exit1:
    StrCpy $R0 $R0 -$R1
  exit2:
    Pop $R2
    Pop $R1
    Exch $R0
FunctionEnd


; *******************************************************************************
; StrContains
; This function does a case sensitive searches for an occurrence of a substring in a string.
; It returns the substring if it is found.
; Otherwise it returns null("").
; Written by kenglish_hi
; Adapted from StrReplace written by dandaman32
; *******************************************************************************
Var STR_HAYSTACK
Var STR_NEEDLE
Var STR_CONTAINS_VAR_1
Var STR_CONTAINS_VAR_2
Var STR_CONTAINS_VAR_3
Var STR_CONTAINS_VAR_4
Var STR_RETURN_VAR

Function StrContains
  Exch $STR_NEEDLE
  Exch 1
  Exch $STR_HAYSTACK
  ; Uncomment to debug
  ;MessageBox MB_OK 'STR_NEEDLE = $STR_NEEDLE STR_HAYSTACK = $STR_HAYSTACK '
    StrCpy $STR_RETURN_VAR ""
    StrCpy $STR_CONTAINS_VAR_1 -1
    StrLen $STR_CONTAINS_VAR_2 $STR_NEEDLE
    StrLen $STR_CONTAINS_VAR_4 $STR_HAYSTACK
    loop:
      IntOp $STR_CONTAINS_VAR_1 $STR_CONTAINS_VAR_1 + 1
      StrCpy $STR_CONTAINS_VAR_3 $STR_HAYSTACK $STR_CONTAINS_VAR_2 $STR_CONTAINS_VAR_1
      StrCmp $STR_CONTAINS_VAR_3 $STR_NEEDLE found
      StrCmp $STR_CONTAINS_VAR_1 $STR_CONTAINS_VAR_4 done
      Goto loop
    found:
      StrCpy $STR_RETURN_VAR $STR_NEEDLE
      Goto done
    done:
   Pop $STR_NEEDLE ;Prevent "invalid opcode" errors and keep the
   Exch $STR_RETURN_VAR
FunctionEnd

!macro _StrContainsConstructor OUT NEEDLE HAYSTACK
  Push `${HAYSTACK}`
  Push `${NEEDLE}`
  Call StrContains
  Pop `${OUT}`
!macroend

!define StrContains '!insertmacro "_StrContainsConstructor"'


; *******************************************************************************
; The stuff to install
; *******************************************************************************
Section "" ;No components page, name is not important

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Checking Java & Components"

  ; This is important to have $APPDATA variable point to ProgramData folder instead of current user's Roaming folder
  SetShellVarContext all

  ; check java is installed
  Goto CheckLocal

  ; 1) Check local JRE
  CheckLocal:
    ClearErrors
    StrCpy $R0 "$INSTDIR\jre\bin\${JAVAEXE}"
    IfFileExists $R0 0 CheckSysdir
    StrCpy $wrapper_java "jre\bin\java"
    Goto JreFound

  ; 2) Check Sysdir
  CheckSysdir:
    ClearErrors
    StrCpy $R0 "$SYSDIR\${JAVAEXE}"
    IfFileExists $R0 0 CheckProfile
    StrCpy $wrapper_java "$SYSDIR\java"
    Goto JreFound

  ; 3) Check Profile
  CheckProfile:
    ClearErrors
    StrCpy $R0 "$PROFILE\${JAVAEXE}"
    IfFileExists $R0 0 CheckJavaHome
    StrCpy $wrapper_java "$PROFILE\java"
    Goto JreFound

  ; 4) Check for JAVA_HOME
  CheckJavaHome:
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckRegistry
    IfFileExists $R0 0 CheckRegistry
    StrCpy $wrapper_java "java"
    Goto JreFound

  ; 5) Check for registry
  CheckRegistry:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckPath
    IfFileExists $R0 0 CheckPath
    StrCpy $wrapper_java "java"
    Goto JreFound

  ; 6) Check Path
  CheckPath:
    ClearErrors
    SearchPath $R0 ${JAVAEXE}
    IfErrors DownloadJRE
    IfFileExists $R0 0 NoJRE
    StrCpy $wrapper_java "java"
    Goto JreFound

  NoJRE:
  MessageBox MB_OK "A compatible Java installation was not found. Please uninstall all java versions, then install Java ${PLATFORM} bit only ${JAVAMIN} or higher and try again. The Java download page will be opened in your default browser."
  Goto DownloadJRE
  LowJRE:
  MessageBox MB_OK "Java Version $0 found but is not compatible. Please uninstall all java versions, then install Java ${PLATFORM} bit only ${JAVAMIN} or higher and try again. The Java download page will be opened in your default browser."
  DownloadJRE:
  ExecShell "open" "https://adoptium.net"
  abort "Install Aborted, Try again after installing Java V${JAVAMIN}+"

  JreFound:
  ; Check The Version Found
  GetDllVersion $R0 $R1 $R2
  IntOp $R3 $R1 / 0x00010000
  IntOp $R4 $R1 & 0x0000FFFF
  IntOp $R5 $R2 / 0x00010000
  IntOp $R6 $R2 & 0x0000FFFF
  StrCpy $0 "$R3.$R4.$R5.$R6"
  Push "$0" ; Input string
  Call MajorVersion
  Pop "$1"

  ;MessageBox MB_OK "Found Major Java Version $1 @ $R0"

  IntCmp $1 ${JAVAMIN} is lessthan morethan
  is:
    Goto JavaMinDone
  lessthan:
    Goto LowJRE
  morethan:
    Goto JavaMinDone
  JavaMinDone:

  DetailPrint "Using Java Version $0 found @ $R0"

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Stopping Tray and Services"

  ; stop the tray app if running
  nsExec::Exec '"taskkill" /f /im Tray.exe'

  ; stop the service if running
  nsExec::Exec '"net" stop TVSCHPRO'

  ;write run reg key to start Tray.exe
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Run" "Tray Icon" "$INSTDIR\win\Tray.exe"

  ; *******************************************************************************
  ; Copy all the files to install path and programdata
  ; *******************************************************************************
  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Copying install files"

  ; service files
  SetOutPath $INSTDIR
  File ..\scheduler\service\wrapper.conf
  File ..\scheduler\service\${PLATFORM}\wrapper.dll
  File ..\scheduler\service\${PLATFORM}\wrapper.exe
  File ..\scheduler\service\${PLATFORM}\wrapper.jar

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Editing Wrapper Config"

  ; Edit wrapper.conf to suit java install
  ClearErrors
  FileOpen $0 "$INSTDIR\wrapper.conf" "r"                                   ; open target file for reading
  GetTempFileName $R0                                                       ; get new temp file name
  FileOpen $1 $R0 "w"                                                       ; open temp file for writing
  loop-wrapperconf:
     FileRead $0 $2                                                         ; read line from target file
     IfErrors done-wrapperconf                                              ; check if end of file reached
     StrCpy $3 $2 21
      StrCmp $3 "wrapper.java.command=" 0 +2
      StrCpy $2 "wrapper.java.command=$wrapper_java$\r$\n"                  ; write the found java in the wrapper
     FileWrite $1 $2                                                        ; write changed or unchanged line to temp file
     Goto loop-wrapperconf
  done-wrapperconf:
     FileClose $0                                                           ; close target file
     FileClose $1                                                           ; close temp file
     Delete "$INSTDIR\wrapper.conf"                                         ; delete target file
     CopyFiles /SILENT $R0 "$INSTDIR\wrapper.conf"                          ; copy temp file to target file
     Delete $R0                                                             ; delete temp file

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Removing old Files"

  ; remove old mail support files
  Delete $INSTDIR\javax*.jar
  Delete $INSTDIR\jakarta*.jar

  ; scheduler base files
  File ..\scheduler\*.jar
  File ..\scheduler\*.txt
  File ..\scheduler\start*.bat
  File ..\scheduler\make-jre-tvsp-only.bat
  File ..\scheduler\xmltv.dtd

  ; http files
  SetOutPath $INSTDIR\http
  File /r /x .svn ..\scheduler\http\*.*

  ; win tools
  SetOutPath $INSTDIR\win
  File /x .svn ..\scheduler\win\*.*
  File /r /x .svn ..\scheduler\win\${PLATFORM}\*.*

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Moving old File Locations"


  ; Move any old data files to correct location
  CreateDirectory "${ProgramData}"
  Rename $INSTDIR\archive "${ProgramData}\archive"
  Rename $INSTDIR\capture "${ProgramData}\capture"
  Rename $INSTDIR\log "${ProgramData}\log"
  Rename $INSTDIR\data\xmltv "${ProgramData}\xmltv"
  CopyFiles /SILENT $INSTDIR\*.prop "${ProgramData}\prop"
  CopyFiles /SILENT $INSTDIR\data\*.prop "${ProgramData}\prop"
  CopyFiles /SILENT $INSTDIR\data\*.list "${ProgramData}\list"
  CopyFiles /SILENT $INSTDIR\data\*.sof "${ProgramData}\sof"
  CopyFiles /SILENT $INSTDIR\data\*.xml "${ProgramData}\xml"

  ; Fix permissions - due to copying/moving as admin/system
  nsExec::Exec 'Icacls "${ProgramData}" /grant Users:(OI)(CI)M'

  ; Remove old archive folder
  RMDir /r $INSTDIR\archive
  ; Remove old capture folder
  RMDir /r $INSTDIR\capture
  ; Remove old data folder
  RMDir /r $INSTDIR\data
  ; Remove old Server.prop (now moved to programdata)
  Delete $INSTDIR\*.prop

  ; new data files to programdata
  SetOutPath "${ProgramData}"
  File /r /x .svn /x *.prop ..\scheduler\data\*.*

  ; Default capture folder
  SetOutPath "${ProgramData}\capture"
  File /x .svn ..\scheduler\capture\*.*

  ; prop files
  SetOutPath "${ProgramData}\prop"
  File ..\scheduler\data\prop\mime-types.prop

  IfFileExists "${ProgramData}\prop\authentication.prop" authPropExists 0
  File ..\scheduler\data\prop\authentication.prop
  authPropExists:

  IfFileExists "${ProgramData}\prop\server.prop" serverPropExists 0
  File /oname=server.prop ..\scheduler\data\prop\server.prop.default
  serverPropExists:

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Updating General Options"

  ; Change Recommended Settings in server.prop
  ClearErrors
  FileOpen $0 "${ProgramData}\prop\server.prop" "r"                         ; open target file for reading
  GetTempFileName $R0                                                       ; get new temp file name
  FileOpen $1 $R0 "w"                                                       ; open temp file for writing
  loop-serverprop:
    FileRead $0 $2                                                          ; read line from target file
    IfErrors done-serverprop                                                ; check if end of file reached
    StrCmp $2 "path.theme=default$\r$\n" 0 +3
      MessageBox MB_YESNO "Change the KB Interface to updated theme (Recommended)?" IDNO +2
      StrCpy $2 "path.theme=calvi$\r$\n"
    StrCmp $2 "path.theme.epg=epg-vertical.xsl$\r$\n" 0 +3
      MessageBox MB_YESNO "Change the EPG Interface to updated theme (Recommended)?" IDNO +2
      StrCpy $2 "path.theme.epg=epg-calvi.xsl$\r$\n"
    StrCmp $2 "path.theme.epg=epg-horizontal.xsl$\r$\n" 0 +3
      MessageBox MB_YESNO "Change the EPG Interface to updated theme (Recommended)?" IDNO +2
      StrCpy $2 "path.theme.epg=epg-calvi.xsl$\r$\n"
    StrCmp $2 "guide.source.file=data\\xmltv$\r$\n" 0 +3
      MessageBox MB_YESNO "Change the EPG Directory to new location (Recommended)?" IDNO +2
      StrCpy $2 "guide.source.file=xmltv$\r$\n"
    ; substring first 9 characters and remove lines that match for old settings no longer used.
    StrCpy $3 $2 9
    StrCmp $3 "path.data" 0 +2
      Goto loop-serverprop
    StrCmp $3 "path.temp" 0 +2
      Goto loop-serverprop
    StrCmp $3 "path.xsl=" 0 +2
      Goto loop-serverprop
    ; substring first 6 characters and remove lines with Title case from old versions.
    StrCpy $4 $2 6
    StrCmpS $4 "AutoDe" 0 +2
      Goto loop-serverprop
    StrCmpS $4 "Captur" 0 +2
      Goto loop-serverprop
    StrCmpS $4 "EPG.Sh" 0 +2
      Goto loop-serverprop
    StrCmpS $4 "Schedu" 0 +2
      Goto loop-serverprop
    StrCmpS $4 "Tasks." 0 +2
      Goto loop-serverprop
    StrCmpS $4 "Server" 0 +2
      Goto loop-serverprop
    ; Same but for 13 characters
    StrCpy $5 $2 13
    StrCmpS $5 "filebrowser.D" 0 +2
      Goto loop-serverprop
    StrCmpS $5 "filebrowser.S" 0 +2
      Goto loop-serverprop
    StrCmpS $5 "server.kbLED=" 0 +2
      Goto loop-serverprop
    FileWrite $1 $2                                                         ; write changed or unchanged line to temp file
    Goto loop-serverprop
  done-serverprop:
     FileClose $0                                                           ; close target file
     FileClose $1                                                           ; close temp file
     Delete "${ProgramData}\prop\server.prop"                               ; delete target file
     CopyFiles /SILENT $R0 "${ProgramData}\prop\server.prop"                ; copy temp file to target file
     Delete $R0                                                             ; delete temp file

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Registering COM Objects"

  ; Register any com filters we need (64bit aware), /S to suppress popups
  nsExec::Exec '"$SYSDIR\regsvr32.exe" "$INSTDIR\win\TSMemoryShare.ax" /S'
  nsExec::Exec '"$SYSDIR\regsvr32.exe" "$INSTDIR\win\FileWriter.ax" /S'

  ; Add start menu items
  CreateDirectory "$SMPROGRAMS\${PROJECTWS}"
  CreateShortCut "$SMPROGRAMS\${PROJECTWS}\Main Page.lnk" "$INSTDIR\win\Tray.exe" "-open_home" "$INSTDIR\win\Tray.exe" 0
  CreateShortCut "$SMPROGRAMS\${PROJECTWS}\Configuration Page.lnk" "$INSTDIR\win\Tray.exe" "-open_config" "$INSTDIR\win\Tray.exe" 0
  CreateShortCut "$SMPROGRAMS\${PROJECTWS}\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0

  ; Calc the size values of the install folders - this will of course change over time.
  ${GetSize} "$INSTDIR" "/S=0K" $1 $2 $3
  ${GetSize} "${ProgramData}" "/S=0K" $4 $5 $6
  IntOp $0 $1 + $4
  IntFmt $0 "0x%08X" $0

  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "${ARP}" "DisplayName" "${PROJECTWS}"
  WriteRegStr HKLM "${ARP}" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "${ARP}" "InstallDirectory" '"$INSTDIR"'
  WriteRegStr HKLM "${ARP}" "DisplayIcon" '"$INSTDIR\win\Tray.exe"'
  WriteRegStr HKLM "${ARP}" "DisplayVersion" "${version}"
  WriteRegDWORD HKLM "${ARP}" "EstimatedSize" "$0"
  WriteRegDWORD HKLM "${ARP}" "NoModify" 1
  WriteRegDWORD HKLM "${ARP}" "NoRepair" 1

  WriteUninstaller "uninstall.exe"

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Starting Tray App"

  ; Start Tray App and use it to check for VC dependencies as well
  traystart:
  ClearErrors
  Exec "$INSTDIR\win\Tray.exe"
  ; Check if its running, if not check registry and suggest download of vc_redist
  FindProcDLL::FindProc "Tray.exe"
  StrCmp $R0 1 traystarted ; Note: FindProcDLL and other plugins has had issues on some machines.
  ; Use tasklist and StrContains as backup to determine if tray.exe running.
  nsExec::ExecToStack 'tasklist /fi "ImageName eq Tray.exe" /nh'
  Pop $0
  Pop $1
  ${StrContains} $2 "Tray.exe" $1
  StrCmp $2 "Tray.exe" traystarted

  ; ExecWait pops up console window, so not desirable
  ; ExecWait 'cmd /c tasklist /fi "ImageName eq Tray.exe" /fo csv 2>NUL | find /I "Tray.exe">NUL' $R0   ; StrCmp $R0 0 traystarted
  ; DetailPrint "Tray Check $R0 $0 $1"
  ; WMI for 32/64 bit, no console window, but has been deprecated windows 11 :(.
  ; nsExec::ExecToStack 'wmic process where name="Tray.exe" get name'

  ; Check for vcredist install, Prompt to download and install if not registered correctly
  ReadRegStr $1 HKLM "SOFTWARE\WOW6432Node\Microsoft\VisualStudio\${VCREDISTVER}\VC\Runtimes\${ARCHITECTURE}" "Installed"
  StrCmp $1 1 vcinstalled
  ; Not installed according to registry, offer to download and install
  MessageBox MB_OKCANCEL "${VCREDISTDES} ${ARCHITECTURE} Redistributable is required, OK to download and install, Cancel to abort?" IDOK  vcdownload IDCANCEL vcabort
  vcdownload:
  NSISdl::download ${VCREDISTURL} "$Temp\vcredist_${ARCHITECTURE}.exe"
  pop $0
  StrCmp $0 "success" vcexec
  MessageBox MB_OKCANCEL "Download of vcredist Failed. OK to retry download?" IDOK vcdownload IDCANCEL vcabort
  vcabort:
  abort "Install Aborted, Try again after installing ${VCREDISTDES} ${ARCHITECTURE}"
  vcexec:
  nsExec::Exec '"$Temp\vcredist_${ARCHITECTURE}.exe" /q' ; '/q' to install silently
  pop $0
  StrCmp $0 0 traystart
  MessageBox MB_OKCANCEL "${VCREDISTDES} Redist failed to install. OK to try and reinstall VC Redist?, Cancel to abort?" IDOK  vcexec IDCANCEL vcabort
  vcinstalled:
  MessageBox MB_OKCANCEL "${VCREDISTDES} Redist installed but Tray.exe failed to start. OK to download and reinstall VC Redist?, Cancel to continue anyway?" IDOK  vcdownload IDCANCEL traystarted
  traystarted:

  ; Remove obsolete files and folders.
  RMDir /r $INSTDIR\win32

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Starting Service"

  ; Install the service and start it, can use -it wrapper option but does not start when already installed then.
  nsExec::Exec '"$INSTDIR\wrapper.exe" -i "$INSTDIR\wrapper.conf"'
  ;ExecWait '"net" start TVSCHPRO'
  nsExec::Exec '"net" start TVSCHPRO'

  ; Future install for other users option
  ;ExecWait '"$INSTDIR\wrapper.exe" -it "$INSTDIR\wrapper.conf" "wrapper.ntservice.account.prompt=True" "wrapper.ntservice.password.prompt=True"'
  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Opening Web Pages"

  ; open main page
  nsExec::Exec '"$INSTDIR\win\Tray.exe" -open_home'
  nsExec::Exec '"$INSTDIR\win\Tray.exe" -open_config'

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Install Completed"


SectionEnd ; end the section

; *******************************************************************************
; Uninstall Stuff below this line
; *******************************************************************************

Section "Uninstall"

  ; This is important to have $APPDATA variable point to ProgramData folder instead of current user's Roaming folder
  SetShellVarContext all
  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Stopping Processes"

  nsExec::Exec '"taskkill" /f /im Tray.exe'
  nsExec::Exec '"taskkill" /f /im StreamProducer.exe'
  nsExec::Exec '"taskkill" /f /im StreamConsumer.exe'

  ; stop & remove the service
  nsExec::Exec '"$INSTDIR\wrapper.exe" -r "$INSTDIR\wrapper.conf"'

  UnRegDLL "$INSTDIR\win\FileWriter.ax"
  UnRegDLL "$INSTDIR\win\TSMemoryShare.ax"

  !insertmacro MUI_HEADER_TEXT "TV Scheduler Pro" "Removing Files & Registry Settings"

  ; Remove directories used
  RMDir /r "$SMPROGRAMS\${PROJECTWS}"
  RMDir /r "$INSTDIR"

  ; Remove registry keys
  DeleteRegValue HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Run" "Tray Icon"
  DeleteRegKey HKLM "${ARP}"

  ; Prompt User if Programdata should be removed
  MessageBox MB_YESNO "Uninstall the User Specific Program Data in ${ProgramData}?" IDNO +2
  RMDir /r "${ProgramData}"

SectionEnd