:loop
::call C:\Programs\Asciidoc\genAsciidoc2Html.bat Vhdl/Java2Vhdl_StyleGuide.adoc ../../../../html/Vhdl
call C:\Programs\Asciidoc\genAsciidoc2Html.bat Vhdl/Java2Vhdl_Approaches.adoc ../../../../html/Vhdl
::call C:\Programs\Asciidoc\genAsciidoc2Html.bat Vhdl/VhdlConv.adoc ../../../../html/Vhdl
echo done
pause
goto :loop

