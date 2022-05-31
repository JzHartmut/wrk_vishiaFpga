echo =========================================================================
echo execute  $0
export DSTNAME="exmplBlinkingLedFpga"
echo " ... generates the $DSTNAME.jar from srcJava_exmplBlinkingLedFpga core sources"

#Do not change the version on repeated build, and check the checksum and content of jar.
#If it is equal, it is a reproduces build. The $VERSIONSTAMP is important 
#  because it determines the timestamp and hence the checksum in the jar file. 
export VERSIONSTAMP="2022-05-31"
## Determine a dedicated vishiaBase-yyyy-mm-dd.jar or deactivate it to use the current vishiaBase.jar:
export VERSION_VISHIABASE="XX2021-07-01"


## The VERSIONSTAMP can come form calling script, elsewhere it is set with the current date.
## This determines the names of the results, but not the content and not the MD5 check sum.
if test "$VERSIONSTAMP" = ""; then export VERSIONSTAMP=$(date -I); fi   ## writes current date
## Determines the timestamp of the files in the jar. The timestamp determines also
## the MD5 check code. 
## Do not change the version on repeated build, and check the checksum and content of jar.
## If it is equal, it is a reproduces build. The $VERSIONSTAMP is important 
##  because it determines the timestamp and hence the checksum in the jar file. 
## Using another timestamp on equal result files forces another MD5.
## Hence let this unchanged in comparison to a pre-version 
## if it is assumed that the sources are unchanged.
## Only then a comparison of MD5 is possible. 
## The comparison byte by byte inside the jar (zip) file is always possible.
## Use this timestamp for file in jars, influences the MD5 check:
export TIMEinJAR="$VERSIONSTAMP+00:00"

export MAKEBASEDIR="."
if ! test -d $MAKEBASEDIR; then
  export MAKEBASEDIR="."
fi

# It should have anytime the stamp of the newest file, independing of the VERSIONSTAMP
export SRCZIPFILE=""

# Select the location and the proper vishiaBase
# for generation with a given timestamp of vishiaBase in the vishia file tree:
if test -f ../../../../../tools/vishiaBase.jar
then export JAR_vishiaBase="../../../../../tools/vishiaBase.jar"
# for generation side beside: 
elif test -f ../../jars/vishiaBase.jar
then export JAR_vishiaBase="../../jars/vishiaBase.jar"
else
  echo vishiaBase.jar not found, abort
  exit
fi
echo JAR_vishisBase=$JAR_vishiaBase

if test "$OS" = "Windows_NT"; then export sepPath=";"; else export sepPath=":"; fi
#The CLASSPATH is used for reference jars for compilation which should be present on running too.
##Note here libs is not really used only for enhancements
export CLASSPATH="$JAR_vishiaBase"

#It is also the tool for zip and jar used inside the core script
export JAR_zipjar="$JAR_vishiaBase"

#determine the sources:
# Note: include sources of vishiaRun are part of the source.zip
export SRC_ALL=".."
export SRC_ALL2="../../srcJava_vishiaFpga"
export SRCPATH="..$sepPath$SRC_ALL2"

# Resourcefiles for files in the jar
export RESOURCEFILES="..:**/*.zbnf ..:**/*.xml ..:**/*.png ..:**/*.txt"


# located from this workingdir as currdir for shell execution:
export MANIFEST=$DSTNAME.manifest
export JARFILE="../../../../../build/$DSTNAME.jar"

#now run the common script:
chmod 777 $MAKEBASEDIR/-makejar-coreScript.sh
$MAKEBASEDIR/-makejar-coreScript.sh


