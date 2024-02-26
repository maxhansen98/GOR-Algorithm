#!/usr/bin/bash 

# get input
proc=$1
usr=$2

# top prints out one refresh as output
# grep usr name
# get all processes
# grep $proc
res=$(top -b -n1 | grep -P "$usr" | awk -F" " '{print $12}' | grep -P "$proc")

# return exit value 0 if process not is running, else 1
if [ -z "$res" ]; then
    exit 0
else
    exit 1
fi
