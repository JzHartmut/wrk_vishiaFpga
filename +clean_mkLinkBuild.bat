echo off
echo called: %0
REM cleans and creates a directory in %TMP% for temporary stuff.
REM recommended: Use a RAM disk for such if you have enough RAM istalled (0.5..1 GByte is usual really sufficient for the RAM disk)
REM a build directory will be created as symbolic link (JUNCTION in MS-Windows) to this temporary folder.
REM The used temporary for the link build
set TD=%TMP%\Example1_BlinkingLed_VHDL_build

call .\+clean.bat nopause

REM TMP should be set in windows, it may refer a RAM disk
REM only emergency if TMP is not set:
if not "%TMP%"=="" goto :tmpOk 
  REM Windows-batch-bug: set inside a if ...(...) does not work!
  echo set TMP=c:\tmp
  set TMP=c:\tmp
  mkdir c:\tmp
:tmpOk
echo TMP=%tmp%

REM clean content if build is not existing, and link
if not exist build (
  REM Note: rmdir /S/Q cleans all, del /S/Q/F does not clean the directory tree
  if exist %TD% rmdir /S/Q %TD% 
  mkdir %TD%
  mklink /J build %TD%
  echo build new created > build/readme.txt
) 

if not "%1"=="nopause" pause
exit /b

