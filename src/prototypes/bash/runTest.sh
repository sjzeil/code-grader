#!/bin/bash
#
#  Prototype for automatic grader.  
#    1. Evaluation is limited to "diff -b"
#    2. Accept/reject only - no partial scoring.
#    3. Filters are limited to OS commands
#
absolute_path () {
    cd "$(dirname "$1")"
    case $(basename "$1") in
        ..) echo "$(dirname $(pwd))";;
        .)  echo "$(pwd)";;
        *)  echo "$(pwd)/$(basename "$1")";;
    esac
}

if [ ! -r $2/$1 ];
then
    echo '***' There is no test in $2/$1
    exit -1
fi

TEST=$1
TESTSDIR="$(absolute_path $2)"
BUILDDIR="$(absolute_path $3)"
RUNASGOLD=$4     # if 1, writes *.expected and *.timelimit, does not compare output

#
#  Collect configuration properties from $TESTSDIR/Defaults/config.yaml and $TESTSDIR/$TEST/config.yaml
#

getProperty () {
    YAML1=($TESTSDIR/$TEST/*.yaml)
    YAML2=($TESTSDIR/*.yaml)
    PROP1=($TESTSDIR/$TEST/*.$1)
    if [ -e ${PROP1[0]-} ];
    then
        PropertyValue=`cat ${PROP1[0]}`
    fi
    if [[ "$PropertyValue" == "" ]];
    then
        if [ -e ${YAML1[0]-} ];
        then
            PropertyValue=`grep -i '^ *'$1: ${YAML1[0]} | sed 's/^[^:]*: *//'`
        fi
    fi
    if [[ "$PropertyValue" == "" ]];
    then
        if [ -e ${YAML2[0]-} ];
        then
            PropertyValue=`grep -i '^ *'$1':' ${YAML2[0]} | sed 's/^[^:]*: *//'`
        fi
    fi
    echo $PropertyValue
}

if [[ "$RUNASGOLD" -ne "1" ]] ;
then
    echo Test $TEST
fi
LAUNCH="$(getProperty 'launch')"
if [[ "$LAUNCH" == "" ]];
then
    echo '***' No launch property -- cannot run the program
    exit -1
fi

PARAMS="$(getProperty 'params')"

FILTER="$(getProperty 'filter')"
if [[ "$FILTER" != "" ]];
then
    FILTER="| $FILTER"
fi

STDERR="$(getProperty 'stderr')"

TIMELIMIT="$(getProperty 'timelimit')"
if [[ "$TIMELIMIT" != "" ]];
then
    TIMELIMIT="$TIMELIMIT"s
fi
TIMELIM=($TESTSDIR/$TEST/*.timelimit)
if [ -e ${TIMELIM[0]-} ];
then
    TIMELIMIT=`cat ${TIMELIM[0]}`
    TIMELIMIT=$(( 4 * $TIMELIMIT ))
    TIMELIMIT=$(( $TIMELIMIT < 1 ? 1: $TIMELIMIT ))s
fi
if [[ "$TIMELIMIT" == "" ]];
then
    TIMELIMIT=1s
fi

#
# Check for $TESTSDIR/$TEST/*.in files
#
INFILES=($TESTSDIR/$TEST/*.in)
if [ -e ${INFILES[0]-} ];
then
    TESTIN=${INFILES[0]}
else
    TESTIN=
fi

EXPFILES=($TESTSDIR/$TEST/*.expected)
if [ -e ${EXPFILES[0]-} ];
then
    EXPECTED=${EXPFILES[0]}
fi

TESTOUT=$TESTSDIR/$TEST/test.out
TESTERR=$TESTSDIR/$TEST/test.err
TESTDIFF=$TESTSDIR/$TEST/test.diff
TESTSCORE=$TESTSDIR/$TEST/test.score
TESTTIME=$TESTSDIR/$TEST/test.time



/bin/rm -f $TESTOUT $TESTERR $TESTDIFF

COMMAND="$LAUNCH $PARAMS"
if [[ "$RUNASGOLD" -ne "1" ]] ;
then
    if [[ "$TESTIN" == "" ]];
    then
        echo '  ' Running $COMMAND
    else
        echo '  ' Running $COMMAND \< $TESTIN
    fi
fi
PWD=`pwd`
cd $BUILDDIR
START=$(date '+%s')
if [[ "$TESTIN" == "" ]];
then
    timeout -s SIGKILL $TIMELIMIT $COMMAND > $TEST.out0 2> $TESTERR
else
    timeout -s SIGKILL $TIMELIMIT $COMMAND < $TESTIN > $TEST.out0 2> $TESTERR
fi
STOP=$(date '+%s')
TIME=$(expr $STOP - $START)
echo $TIME > $TESTTIME
STATUS=$?
cd $PWD

# echo STATUS was $STATUS
if (( $STATUS ==  137 ));
then
    echo '***' Program still running after $TIME elapsed.
    echo '***' "Possible infinite loop (or just poor algorithm design.)"
    echo '***' Timed out >> $test0.out
    echo 0 > $TESTSCORE
else
if (( $STATUS !=  0 ));
then
    echo '***' Program exited abnormally with code $STATUS
    echo '***' Program exited abnormally with code $STATUS >> $TEST.out0
    echo 0 > $TESTSCORE
fi
fi
if [[ "$STDERR" == "" ]];
then
    cat $TEST.out0 $FILTER > $TESTOUT
else
    sed -i 's/^/*** /' $TESTERR
    cat $TEST.out0 $TESTERR $FILTER > $TESTOUT
fi
rm -f $TEST.out0


if [[ "$RUNASGOLD" -eq "1" ]] ;
then
    mv $TESTOUT $TESTSDIR/$TEST/$TEST.expected
    mv $TESTTIME $TESTSDIR/$TEST/$TEST.timelimit
    exit 0
fi


if [[ -r $EXPECTED ]] ;
then
    if diff -b $TESTOUT $EXPECTED > $TESTDIFF;
    then
        echo '***' Passed $TEST
        /bin/rm $TESTDIFF
        echo 100 > $TESTSCORE
    else
        echo '***' $TEST output does not match $TEST.expected
        echo See $TEST.diff for details
        echo 0 > $TESTSCORE
        exit 1
    fi
else
    echo \(cannot check output for $TEST - no $TEST.expected file\)
    exit $STATUS
fi
