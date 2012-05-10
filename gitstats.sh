#!/bin/bash

usage() {
  echo "Usage: `basename $0` [-f] directory"
  exit 1
}

while getopts ":f" opt; do
  case $opt in
    f)
      RUNFETCH=yes
      ;;
    \?)
      usage
      ;;
  esac
done
shift $((OPTIND-1))

if [ $# != 1 ]; then
  usage
fi

upstream() {
  count="$(git rev-list --count --left-right @{upstream}...HEAD 2>/dev/null)"
  regex="(.*)	(.*)"
  if [[ "$count" =~ $regex ]]; then
    #echo "$BASH_REMATCH"
    ucount="${BASH_REMATCH[1]}"
    lcount="${BASH_REMATCH[2]}"
    printf "%8.8s %8.8s" "$ucount" "$lcount"
  else
    printf "%8.8s %8.8s" "-" "-"
  fi
  return
}

lsuntrackedfiles() {
  printf "%10.10s" $(git ls-files --others --exclude-standard| wc -l)
}

showbranch() {
  local b=''
  b=$(git symbolic-ref -q HEAD) || b='(detached HEAD)'
  b=${b##refs/heads/}
  printf "%-15.15s" "${b}"
}

showstaged() {
  local w=''
  local i=''
  git diff --no-ext-diff --quiet --exit-code || w='*'
  if git rev-parse --quiet --verify HEAD >/dev/null; then
    git diff-index --cached --quiet HEAD -- || i='+'
  else
    i='#'
  fi
  printf "%10.10s" "${w}${i}"
}

printf "%-25.25s" "Path"
printf "%-15.15s" "branch"
printf "%10.10s" "stage"
printf "%10.10s" "untracked"
printf "%8.8s %8.8s" "origin" "local"
echo


find "${1}" -name .git -type d -print0 | while read -r -d $'\0' gitdirs
do
  project=`dirname "$gitdirs"`
  # Not needed really"
  if [ -d "${project}" -a -d "${project}/.git" ]; then
    pushd "${project}" >/dev/null; 
    if [ ! -z $RUNFETCH ]; then
      git fetch -q > /dev/null >& /dev/null
    fi
    printf "%-25.25s" "$(basename "${project}")"
    showbranch
    showstaged
    lsuntrackedfiles
    upstream 
    echo
    popd >/dev/null
  fi
done
