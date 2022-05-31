REM Java as JRE8 should be available.

REM get depending files from internet, accept if they are existing already, but check MD5
REM bom is the bill of material, what is gotten from wwww
REM Note: vishiaMinisys.jar is part of this distribution because it is initially necessary, but the MD5 is checked.
set SCRIPTDIR=%~d0%~p0
if exist ..\tools goto :cdok
  if not exist ..\..\src\load_tools (
    echo this is a faulty folder, restore the original
    pause
    exit /b
  )
  if not exist ..\..\tools mkdir ..\..\tools
  copy * ..\..\tools
  cd ..\..\tools
:cdok

java -cp %SCRIPTDIR%/vishiaMinisys.jar org.vishia.minisys.GetWebfile @%SCRIPTDIR%/tools.bom .\

pause


