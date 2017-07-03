#!/usr/bin/env bash

###  ------------------------------- ###
###  Helper methods for BASH scripts ###
###  ------------------------------- ###

whoami=`basename $0`
die() {
  echo "$@" 1>&2
  exit 1
}
# The full qualified directory where this script is located
get_script_dir() {
  # Default is current directory
  local dir=`dirname "$0"`
  local full_dir=`cd "${dir}" ; pwd`
  echo ${full_dir}
}
# Detect if we should use JAVA_HOME or just try PATH.
get_java_cmd() {
  if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "$JAVA_HOME/bin/java"
  else
    echo "java"
  fi
}
echoerr () {
  echo 1>&2 "$@"
}
dlog () {
  [[ $debug ]] && echoerr "$@"
}
execRunner () {
  # print the arguments one to a line, quoting any containing spaces
  [[ $verbose || $debug ]] && echo "# Executing command line:" && {
    for arg; do
      if printf "%s\n" "$arg" | grep -q ' '; then
        printf "\"%s\"\n" "$arg"
      else
        printf "%s\n" "$arg"
      fi
    done
    echo ""
  }
  # we use "exec" here for our pids to be accurate.
  exec "$@" > /dev/null 2>&1 &
  echo $! > $app_home/bin/teclan-lvzaotou.pid
}
addJava () {
  dlog "[addJava] arg = '$1'"
  java_args+=( "$1" )
}
addApp () {
  dlog "[addApp] arg = '$1'"
  app_commands+=( "$1" )
}
addDebugger () {
  addJava "-Xdebug"
  addJava "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$1"
}

# a ham-fisted attempt to move some memory settings in concert
# so they need not be messed around with individually.
get_mem_opts () {
  local mem=${1:-1024}
  local perm=$(( $mem / 4 ))
  (( $perm > 256 )) || perm=256
  (( $perm < 1024 )) || perm=1024
  local codecache=$(( $perm / 2 ))
  # if we detect any of these settings in ${java_opts} we need to NOT output our settings.
  # The reason is the Xms/Xmx, if they don't line up, cause errors.
  if [[ "${java_opts}" == *-Xmx* ]] ||
     [[ "${java_opts}" == *-Xms* ]] ||
     [[ "${java_opts}" == *-XX:MaxPermSize* ]] ||
     [[ "${java_opts}" == *-XX:ReservedCodeCacheSize* ]] ||
     # check java arguments for settings, too
     [[ "${java_args[@]}" == *-Xmx* ]] ||
     [[ "${java_args[@]}" == *-Xms* ]] ||
     [[ "${java_args[@]}" == *-XX:MaxPermSize* ]] ||
     [[ "${java_args[@]}" == *-XX:ReservedCodeCacheSize* ]];
  then
    echo ""
  elif [[ !$no_version_check ]] && [[ "$java_version" > "1.8" ]]; then
    echo "-Xms${mem}m -Xmx${mem}m -XX:ReservedCodeCacheSize=${codecache}m"
  else
    echo "-Xms${mem}m -Xmx${mem}m -XX:MaxPermSize=${perm}m -XX:ReservedCodeCacheSize=${codecache}m"
  fi
}
require_arg () {
  local type="$1"
  local opt="$2"
  local arg="$3"
  if [[ -z "$arg" ]] || [[ "${arg:0:1}" == "-" ]]; then
    die "$opt requires <$type> argument"
  fi
}

# Processes incoming arguments and places them in appropriate global variables.  called by the run method.
process_args () {
  local no_more_snp_opts=0
  while [[ $# -gt 0 ]]; do
    case "$1" in
             --) shift && no_more_snp_opts=1 && break ;;
       -h|-help) usage; exit 1 ;;
    -v|-verbose) verbose=1 && shift ;;
      -d|-debug) debug=1 && shift ;;
    -no-version-check) no_version_check=1 && shift ;;
           -mem) require_arg integer "$1" "$2" && app_mem="$2" && shift 2 ;;
     -jvm-debug) require_arg port "$1" "$2" && addDebugger $2 && shift 2 ;;
     -java-home) require_arg path "$1" "$2" && java_cmd="$2/bin/java" && shift 2 ;;
            -D*) addJava "$1" && shift ;;
            -J*) addJava "${1:2}" && shift ;;
              *) addResidual "$1" && shift ;;
    esac
  done
  if [[ no_more_snp_opts ]]; then
    while [[ $# -gt 0 ]]; do
      addResidual "$1" && shift
    done
  fi
}

# Actually runs the script.
run() {
  # TODO - check for sane environment
  # process the combined args, then reset "$@" to the residuals
  process_args "$@"
  argumentCount=$#
  # check java version
  if [[ ! $no_version_check ]]; then
    java_version_check
  fi
  # Now we check to see if there are any java opts on the environemnt. These get listed first, with the script able to override them.
  if [[ "$JAVA_OPTS" != "" ]]; then
    java_opts="${JAVA_OPTS}"
  fi
  # run sbt
  execRunner "$java_cmd" \
    $(get_mem_opts $app_mem) \
    ${java_opts[@]} \
    "${java_args[@]}" \
    -cp "$app_classpath" \
    $app_mainclass \
    "${app_commands[@]}"
  local exit_code=$?
  exit $exit_code
}

# Now check to see if it's a good enough version
# TODO - Check to see if we have a configured default java version, otherwise use 1.6
java_version_check() {
  readonly java_version=$("$java_cmd" -version 2>&1 | awk -F '"' '/version/ {print $2}')
  if [[ "$java_version" == "" ]]; then
    echo
    echo No java installations was detected.
    echo Please go to http://www.java.com/getjava/ and download
    echo
    exit 1
  elif [[ ! "$java_version" > "1.8" ]]; then
    echo
    echo The java installation you have is not up to date
    echo $app_name requires at least version 1.8+, you have
    echo version $java_version
    echo
    echo Please go to http://www.java.com/getjava/ and download
    echo a valid Java Runtime and install before running $app_name.
    echo
    exit 1
  fi
}

###  ------------------------------- ###
###  Start of customized settings    ###
###  ------------------------------- ###

usage() {
 cat <<EOM
Usage: $script_name [options]
  -h | -help         print this message
  -v | -verbose      this runner is chattier
  -no-version-check  Don't run the java version check.
  -mem <integer>     set memory options in MB (default: $sbt_mem, which is $(get_mem_opts $sbt_mem))
  -jvm-debug <port>  Turn on JVM debugging, open at the given port.
  # java version (default: java from PATH, currently $(java -version 2>&1 | grep version))
  -java-home <path>         alternate JAVA_HOME
  # jvm options and output control
  JAVA_OPTS          environment variable, if unset uses "$java_opts"
  -Dkey=val          pass -Dkey=val directly to the java runtime
  -J-X               pass option -X directly to the java runtime
                     (-J is stripped)
  # special option
  --                 To stop parsing built-in commands from the rest of the command-line.
                     e.g.) enabling debug and sending -d as app argument
                     \$ ./start-script -d -- -d
In the case of duplicated or conflicting options, basically the order above
shows precedence: JAVA_OPTS lowest, command line options highest except "--".
EOM
}

###  ------------------------------- ###
###  Main script                     ###
###  ------------------------------- ###

declare -a java_args
declare -a app_commands
declare -r script_dir="$(get_script_dir "$0")"
declare -r app_home="$(cd ${script_dir}/..; pwd -P)"
declare -r lib_dir="${app_home}/lib"
declare -r conf_dir="${app_home}/conf"
declare -r app_mainclass="teclan.lvzaotou.example.Main"
for i in $lib_dir/*.jar; do
  libs=$libs:$i
done
declare -r app_classpath="${conf_dir}:$libs"
addJava "-Duser.dir=${app_home}"
addJava "-Dapp.home=${app_home}"
if [ -d $app_home/jre ]; then
  JAVA_HOME=$app_home/jre
fi
# java_cmd is overrode in process_args when -java-home is used
declare java_cmd=$(get_java_cmd)
run "$@"
