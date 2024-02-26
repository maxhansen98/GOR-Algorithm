#!/usr/bin/bash 

# get input
proc=$1
usr=$2

# top prints out one refresh as output
# grep usr name
# get all processes
# grep $proc
# found a better command on stackoverflow
# res=$(top -b -n1 | grep -Pw "$usr" | awk '{print $12}' | grep -Pw "$proc")
res=$(ps -u "$usr" -o cmd | grep -Pw "$proc")


# return exit value 0 if process not is running, else 1
if [ -z "$res" ]; then
    # echo "Process $proc is not running"
    exit 0
else
    # echo "Process $proc is running"
    exit 1
fi
