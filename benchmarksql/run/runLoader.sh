#!/usr/bin/env bash

if [ $# -lt 1 ] ; then
    echo "usage: $(basename $0) PROPS_FILE [ARGS]" >&2
    exit 2
fi

source funcs.sh $1
shift

myOPTS="-Dprop=${PROPS}"
myOPTS="${myOPTS} -Djava.security.egd=file:/dev/./urandom"

java -cp "../build/libs/*:../libs/*" $myOPTS LoadData.LoadData $*
