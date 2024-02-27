#!/usr/bin/bash 

# get input
proc=$1
usr=$2

# top prints out one refresh as output
# grep usr name
# get all processes
# grep $proc
# setting the delay to be very short 
# res=$(top -b -d0.1 -n10 | grep -Pw "$usr" | grep -Pw "$proc")

# didn't know this existed :)
res=$(pgrep -u $usr $proc)

# return exit value 0 if process not is running, else 1
if [ -z "$res" ]; then
    exit 0
else
    exit 1
fi
