@echo off
echo called: %0
REM See www.vishia.org/Fpga/index.html
REM This batchfile translates the template files to VHDL.
REM The template files are used for the manual to explain, you should find the same but more content in this template files.
REM Hint: For editing Java in Eclipse you may unpack the tools/vishiaBase-2022-04-21-source.zip and include it in the sources.
REM Additionally you can use it as zip library in Eclipse too.

REM the working directory is the root directory where src/... and build/... is located
::it is linux shell: cd `dirname "$0"`/../../..
cd %~d0%~p0\..\..\..
echo currentDir=%CD%

if not exist build call +clean_mkLinkBuild.bat nopause
if not exist build mkdir build
if not exist tools (
  echo to load the tools, the jar files for translation
  echo go to src/load_tools/+checkAndLoadTools.bat
  pause
  exit /b
)

REM The both jar files should be loaded with +checkAndLoadTools.bat. 
java -cp tools/vishiaBase.jar;tools/vishiaVhdlConv.jar org.vishia.java2Vhdl.Java2Vhdl --@%0:convArgs 
REM info: one space after the label, then trim all trailing spaces also without comment
REM --- is a commented argument
::convArgs ##             
::-sdir:src/vishiaFpga/java                    ##source dirs relative to working dir
::-sdir:src/vishiaFpga/java                    ##more as one possible, also with absolute path
::-top:org.vishia.fpga.tmpl_J2Vhdl.MyFpgaTop   ##top level file to translate in first source dir
::-o:build/Test_MyFpga.vhd                     ##output
::-oc:build/Test_MyFpga.lpfX                   ##constraint to merge (time cell groups)
::-tmp:build/  
::---parseData                                 ## The java data tree to view
::---parseResult                               ## The parse result list
::---parseLog                                  ## an elaborately parse log
::-rep:build/Test_MyFpga_report.txt            ##report with meta information
pause

