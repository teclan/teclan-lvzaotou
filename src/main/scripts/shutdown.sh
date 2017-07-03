#!/bin/sh

#
# Get the APP_HOME
#
whoami=`basename $0`
whereami=`echo $0 | sed -e "s#^[^/]#\`pwd\`/&#"`
whereami=`dirname $whereami`

# Resolve any symlinks of the now absolute path, $whereami
realpath_listing=`ls -l $whereami/$whoami`
case "$realpath_listing" in
*-\>\ /*)
  realpath=`echo $realpath_listing | sed -e "s#^.*-> ##"`
;;
*-\>*)
  realpath=`echo $realpath_listing | sed -e "s#^.*-> #$whereami/#"`
;;
*)
  realpath=$whereami/$whoami
;;
esac
APP_HOME=`dirname "$realpath"`

PID=`cat $APP_HOME/teclan-lvzaotou.pid`

kill -9 $PID
