#!/bin/bash

ipaddr='10.0.1.1'
oraversion='1.0.1-SNAPSHOT'

function helpFn {
  echo 'THIS SCRIPT IS FOR DEVELOPERS, WHO PULLED THE GIT-REPOSITORY AND WORK WITH THE OpenRoberta ARTIFACT';
  echo 'use: ora.sh --CMD1 OPT_PARM1 --CMD2 OPT_PARM2 ... ...';
  echo '';
  echo 'you may customize the script by defining default values. Values are now:';
  echo 'ipaddr='"$ipaddr";
  echo 'oraversion='"$oraversion";
  echo '';
  echo 'miscelleneous commands';
  echo '';
  echo '  --help                             get help';
  echo '  --ipaddr {IP-ADDR}                 set the ip addr of the EV3 brick for further commands';
  echo '  --version {VERSION}                set the version to something like d.d.d where d is one or more digits, e.g. 1.0.1'
  echo '';
  echo 'commands available after a successful mvn clean install';
  echo '';
  echo '  --start {PROPERTY-FILE}            start of the server, optionally supply a property file';
  echo '                                     the default property file is in OpenRobertaServer/src/main/resources';
  echo '                                     a java7 installation is required, java+javac must be in the path' ;
  echo '  --scpev3menu                       scp the ev3menu.jar to the EV3, uses ipaddr';
  echo '                                     the root password is "", thus hit return if you are asked';
  echo '  --scpev3libs                       scp libraries to the EV3, uses ipaddr';
  echo '                                     the root password is "", thus hit return everytime you are asked';
  echo '  --createemptydb PATH-TO-DB         create an empty database with all tables needed for the OpenRoberta server'
  echo '                                     Needs a file name. Creates files and a directory with this name AS PREFIX.'
  echo '                                     if the database exists, it is not recreated. If a table "PROGRAM" is found'
  echo '                                     in an existing database, it is assumed, that the setup has already been done.'
  echo '  --export PATH-TO-INSTALLATION-DIR  create a self-contained installation with an empty database'
  echo '                                     The installation contains a top level start script "start.sh",'
  echo '                                     that can be used without parameter or with a property file as parameter.'
}

function startFn {
  propfile="$1"
  main='de.fhg.iais.roberta.main.ServerStarter'
  run='java -cp "../Resources/resources/*" '"${main} ${propfile}"
  echo "executing: $run"
  cd OpenRobertaServer
  eval $run
}

function check64bit7jdk {
  javaversion=`java -version 2>&1`
  echo
  echo 'you are using the following java runtime:'
  echo $javaversion
  echo
  case "$javaversion" in
    *64-Bit*) : ;;
    *)        echo '**********************************************************************************************'
              echo '* Using the PATH java is resolved to a 32 bit version. The server needs a 64 bit jdk. Exit 4 *'
              echo '**********************************************************************************************'
              exit 4 ;;
  esac
  case "$javaversion" in
    *1\.7\.*) : ;;
    *)        echo '**********************************************************************************************'
              echo '* Using the PATH java is not resolved to version 7. The server needs a version 7 jdk. Exit 4 *'
              echo '**********************************************************************************************'
              exit 4 ;;
  esac
}

function exportApplication {
  exportpath="$1"
  if [[ -e "$exportpath" ]]; then
     echo "target directory \"$exportpath\" already exists - exit 4"
     exit 4
  fi
  echo "creating the target directory \"$exportpath\""
  mkdir "$exportpath"
  echo "creating an empty data base"
  $0 --createemptydb "${exportpath}/db/openroberta-db"
  echo "copying the web resources"
  webresources="OpenRobertaServer/staticResources"
  cp -r "$webresources" "$exportpath"
  echo 'creating directories for user programs and resources'
  mkdir "${exportpath}/userProjects"
  mkdir "${exportpath}/resources"
  mkdir "${exportpath}/updateResources"
  echo "copying resources"
  cp OpenRobertaRuntime/crosscompiler-ev3-build.xml "${exportpath}"
  cp Resources/updateResources/*.jar "$exportpath/updateResources"
  cp Resources/resources/*.jar "$exportpath/resources"
  echo 'creating the start scripts "start.sh" and "start.bat"'
# -------------- begin of here documents --------------------------------------------------
  cat >"${exportpath}/openRoberta.properties" <<.eof
server.jetty.port = 1999

# the following properties are retrieved from the parent pom.xml. They are used to guarantee that
# - the versions of the jars in the server
# - the versions of the jars on the robot
# - the version of the user program jar (generated on the server and transmitted to the robot) and the version of the jars on the robot match
# Note, that in every jar there is a top-level property file that contains the version at the time of compiling the classes contained in the jar
version = ${oraversion}
validversionrange.From = ${oraversion}
validversionrange.To = ${oraversion}

# directory to store (temporarily) the generated user programs
crosscompiler.basedir = userProjects/
# the ant script that uses the cross compiler and jar building tools to build the jar containing the user program
crosscompiler.build.xml = crosscompiler-ev3-build.xml

# the URL of the database
hibernate.connection.url = jdbc:hsqldb:file:db/openroberta-db

# the brick update rest service and the cross compiler need a directory in which all jars/resources for updating are stored
robot.resources.dir = updateResources

.eof
  cat >"${exportpath}/start.sh" <<.eof
#!/bin/bash
javaversion=\`java -version 2>&1\`
case "\$javaversion" in
  *64-Bit*) : ;;
  *) echo 'java is resolved to a 32 bit version. The server needs a 64 bit jdk. Exit 4.'
     exit 4 ;;
esac
case "\$javaversion" in
  *1\\.7\\.*) : ;;
  *) echo 'java is not resolved to a version 7. The server needs a version 7 jdk. Exit 4.'
     exit 4 ;;
esac
properties="\$1"
if [[ "\$properties" == "" ]]; then
   properties="openRoberta.properties"
fi
run="java -cp \"resources/*\" de.fhg.iais.roberta.main.ServerStarter file:\$properties"
echo "executing: \$run"
eval \$run
.eof
  cat >"${exportpath}/start.bat" <<.eof
rem this start.bat uses the C:\Windows\System32\java.exe to start the server.
rem It passes a 1.7 argument to let this exe select the right java installation.
rem If this does not work on your system, you have to tweak the path variable.
rem For local installations inspect your firewall settings, if the robot cannot connect.
@echo off
java -version:"1.7" -version 2>&1 | find "64-Bit" >nul:
if errorlevel 1 (
  echo java is resolved to a 32 bit version. The server needs a 64 bit jdk. Exit 4.
  pause
  exit 4
)
java -version:"1.7" -version 2>&1 | find "1.7." >nul:
if errorlevel 1 (
  echo java is not resolved to a version 7. The server needs a version 7 jdk. Exit 4.
  pause
  exit 4
)
set "PROPERTIES=%1"
if not "%PROPERTIES%" == "" goto gotProperties
set "PROPERTIES=openRoberta.properties"
:gotProperties
set RUN=java -version:"1.7" -cp "resources\*" de.fhg.iais.roberta.main.ServerStarter file:%PROPERTIES%
echo executing: "%RUN%"
%RUN%
.eof
# -------------- end of here documents ----------------------------------------------------
}

function createemptydb {
  dbpath="$1"
  main='de.fhg.iais.roberta.main.Administration'
  run='java -cp "Resources/resources/*" '"${main} createemptydb ${dbpath}"
  echo "executing: $run"
  eval $run
}

function scpev3menuFn {
  run="scp Resources/updateResources/EV3Menu.jar root@${ipaddr}:/home/root/lejos/bin/utils"
  echo "executing: ${run}"
  $run
}

function scpev3libsFn {
  runtime="Resources/updateResources/OpenRobertaRuntime.jar"
  shared="Resources/updateResources/OpenRobertaShared.jar"
  json='Resources/updateResources/json.jar'
  run="ssh root@${ipaddr} mkdir -p /home/roberta/lib"
  echo "executing: ${run}"
  $run
  run="scp ${runtime} ${shared} ${json} root@${ipaddr}:/home/roberta/lib"
  echo "executing: ${run}"
  $run
}

# ---------------------------------------- begin of the script ----------------------------------------------------
cmd="$1"; shift

if [[ "$cmd" == '' ]]; then
   helpFn
   exit 0
fi
check64bit7jdk
while [[ "$cmd" != '' ]]; do
   case "$cmd" in
   --help|-h)          helpFn ;;
   --ipaddr|-i)        ipaddr="$1"; shift ;;
   --version|-v)       oraversion="$1"; shift ;;
   --scpev3menu|-m)    scpev3menuFn ;;
   --scpev3libs|-l)    scpev3libsFn ;;
   --createemptydb|-c) dbpath="$1"; shift
                       createemptydb "$dbpath" ;;
   --export|-e)        exportpath="$1"; shift
                       exportApplication "$exportpath" ;;
   --start|-s)         cmd="$1"; shift
                       if [[ $cmd != '' && $cmd != -* ]]; then
                          propfile="file:$cmd"
                          cmd="$1"; shift
                       fi
                       startFn "$propfile"
                       continue ;; # because cmd is already set
   *)                  echo 'invalid or no parameter: "'"$cmd"'"'
                       echo ''
                       helpFn
                       exit 4 ;;
   esac

   cmd="$1" # here all commands w.o. parameters arrive. Thus cmd has to be set
   shift
done

exit 0