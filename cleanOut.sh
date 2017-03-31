#!/usr/bin/env bash

if [ -z "$1" ]; then
    echo "No file specified"
    exit
fi

echo "doing "$1

file=$1.clean


cat $1 | sed '1,2d'  |  awk '{print $1 " " $2}'
