@echo off
REM This batchfile translates the given example to VHDL.
REM Hint: For editing Java in Eclipse you may unpack the tools/vishiaBase-2022-04-21-source.zip and include it in the sources.
REM Additionally you can use it as zip library in Eclipse too.

if not exist build call +clean_mkLinkBuild.bat nopause
if not exist build mkdir build
if not exist tools (
  echo to load the tools, the jar files for translation
  echo go to src/load_tools/+checkAndLoadTools.bat
  pause
  exit /b
)

REM The vishiaBase.jar contains the whole Java2Vhdl translater beside other stuff. See www.vishia.org/Java/index.html
java -cp tools/vishiaBase.jar;tools/vishiaVhdlConv.jar org.vishia.java2Vhdl.Java2Vhdl --@%0:convArgs 
REM info: one space after the label, then trim all trailing spaces also without comment
REM --- is a commented argument
::convArgs ##             
::-sdir:src/main/java/srcJava_FpgaExmplBlinkingLed            ##source dirs from current
::-sdir:src/main/java/srcJava_vishiaFpga  
::-top:org.vishia.fpga.exmplBlinkingLed.fpgatop.BlinkingLed_Fpga   ##top level file to translate in first source dir
::-o:build/BlinkingLed_Fpga.vhd                               ##output
::-tmp:build/  
::---parseData                                                  ## The java data tree to view
::---parseResult                                                ## The parse result list
::---parseLog                                                 ## an elaborately parse log
::-rep:build/BlinkingLed2Vhdl_report.txt                      ##report with meta information
pause


