#!/bin/bash

#########################################################################
#                                                                       #
#                      Scalable Server Design                           #
#                                                                       #
#          Jason D Stock - stock - 830635765 - Feb 28, 2019             #
#                                                                       #
#########################################################################

# Configurations

DIR="$( cd "$( dirname "$0" )" && pwd )"
BUILD="$DIR/build/classes/java/main"
COMPILE="$( ps -ef | grep [c]s455.scaling.server.Server )"

HOST=indianapolis
PORT=5001
RATE=4

POOL_SIZE=10
BATCH_SIZE=50
BATCH_TIME=5

# Launch Server

LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
echo Project has "$LINES" lines
#gradle clean; gradle build
gnome-terminal --geometry=132x43 -e "ssh -t $HOST 'cd $BUILD; java -cp . cs455.scaling.server.Server $PORT $POOL_SIZE $BATCH_SIZE $BATCH_TIME; bash;'"

sleep 3

# Launch Clients

SCRIPT="cd $BUILD; java -cp . cs455.scaling.client.Client $HOST $PORT $RATE"

for ((j=0; j<${1:-1}; j++))
do
    COMMAND='gnome-terminal'
    for i in `cat machine_list`
    do
        echo 'logging into '$i
        OPTION='--tab -e "ssh -t '$i' '$SCRIPT'"'
        COMMAND+=" $OPTION"
    done
    eval $COMMAND &
done
