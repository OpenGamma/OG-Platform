#!/bin/bash

PROJECT_DIR=projects
TEMP_GIT_STATUS_FILE=$HOME/tmp-gitstatus

function report_git_status () {
  PROJECT_NAME=$1
  STATUS=
  git status > $TEMP_GIT_STATUS_FILE
  [ "$STATUS" == "" ] && STATUS=`grep "# Your branch is ahead " $TEMP_GIT_STATUS_FILE`
  [ "$STATUS" == "" ] && STATUS=`grep "# Changed but not updated" $TEMP_GIT_STATUS_FILE`
  [ "$STATUS" == "" ] && STATUS=`grep "# Untracked files" $TEMP_GIT_STATUS_FILE`
  rm $TEMP_GIT_STATUS_FILE
  if [ "$STATUS" != "" ] ; then
    echo -e $PROJECT_NAME\\t$STATUS
  fi
}

report_git_status OG-Build
cd $PROJECT_DIR
for PROJECT in `ls -1 .`
do
  cd $PROJECT
  report_git_status $PROJECT
  cd ..
done
cd ..
