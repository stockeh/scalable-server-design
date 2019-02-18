HOST=localhost
PORT=5001
RATE=2

POOL_SIZE=10
BATCH_SIZE=5
BATCH_TIME=5

DIR="$( cd "$( dirname "$0" )" && pwd )"
BUILD="$DIR/build/classes/java/main"
COMPILE="$( ps -ef | grep [c]s455.scaling.server.Server )"

SCRIPT="cd $BUILD; java -cp . cs455.scaling.client.Client $HOST $PORT $RATE;"

function new_tab() {
    osascript \
        -e "tell application \"Terminal\"" \
        -e "tell application \"System Events\" to keystroke \"t\" using {command down}" \
        -e "do script \"$SCRIPT\" in front window" \
        -e "end tell" > /dev/null
}

if [ -z "$COMPILE" ]
then
LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
    echo Project has "$LINES" lines
    gradle clean; gradle build
    open -a Terminal .
    pushd $BUILD; java -cp . cs455.scaling.server.Server $PORT $POOL_SIZE $BATCH_SIZE $BATCH_TIME; popd;
else
    for tab in {1..2}
    do
        new_tab
    done
    eval $SCRIPT
fi
