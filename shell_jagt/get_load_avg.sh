#!/bin/bash bash

client=$1

# connect to $client and ignore errors / warnings
load=$(ssh -i ~/.ssh/cip weyrichm@"$client.cip.ifi.lmu.de" "cat /proc/loadavg" 2>/dev/null)

# get the load avg of the last 5 minutes
avg_5=$(echo $load | awk '{print $2}')

# get num of processes
processes=$(echo $load | awk '{print $4}')

# split processes into active and total
active=$(echo $processes | awk -F'/' '{print $1}')
total=$(echo $processes | awk -F'/' '{print $2}')
remaining=$(($total - $active))

out=$"
Client: $client\nLoad avg over the last 5 min: $avg_5\nNumber of active processes: $active\nRemaining capacity: $remaining
"

echo -e $out

