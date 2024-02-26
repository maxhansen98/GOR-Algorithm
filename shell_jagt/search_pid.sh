#!/usr/bin/env bash

# get all args
while getopts ':h:u:p:o:' opt; do
  case $opt in
    h) HOPT=$OPTARG ;;
    u) UOPT=$OPTARG ;;
    p) POPT=$OPTARG ;;
    o) OOPT=$OPTARG ;;
  esac
done

hosts=$HOPT
usr=$UOPT
process=$POPT
out=$OOPT

# this is a hard coded path of my isrunning_script
# othherwise i would'ha had to somehow execute this local script on the ssh connection
isrunning_script="~/local_scripts/isrunning.sh"


# main loop iterating over host names in $hosts
cat "$hosts" | while IFS= read -r host; do
    # Process each line here
    echo "$host"
    client_out=$(ssh -i ~/.ssh/cip weyrichm@"$host.cip.ifi.lmu.de" "cat /proc/loadavg")
    echo $client_out
    # exit 1
    # load=$(ssh -i ~/.ssh/cip weyrichm@"$host" bash "$isrunning_script")
done

