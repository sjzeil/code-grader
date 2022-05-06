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
    if [ -r $TESTSDIR/$TEST/$TEST.yaml ];
    then
        TMP=`grep -i '^ *'$1: $TESTSDIR/$TEST/$TEST.yaml | sed 's/^[^:]*: *//'`
    fi
    if [[ "$TMP" == "" ]];
    then
        if [ -r $TESTSDIR/testing.yaml ];
        then
            TMP=`grep -i '^ *'$1':' $TESTSDIR/testing.yaml | sed 's/^[^:]*: *//'`
        fi
    fi
    echo $TMP
}

echo Test $TEST
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
if [ -r $TESTSDIR/$TEST/$TEST.timelimit ];
then
    TIMELIMIT=`cat $TESTSDIR/$TEST/$TEST.timelimit`
    TIMELIMIT=$(( 4 * $TIMELIMIT ))
    TIMELIMIT=$(( $TIMELIMIT < 1 ? 1: $TIMELIMIT ))s
fi
if [[ "$TIMELIMIT" == "" ]];
then
    TIMELIMIT=1s
fi

#
# Check for $TESTSDIR/$TEST/$TEST.* files
#
if [ -r $TESTSDIR/$TEST/$TEST.in ];
then
    TESTIN=" < $TESTSDIR/$TEST/$TEST.in"
else
    TESTIN=
fi

if [ -r $TESTSDIR/$TEST/$TEST.expected ];
then
    EXPECTED="$TESTSDIR/$TEST/$TEST.expected"
fi

TESTOUT=$TESTSDIR/$TEST/$TEST.out
TESTERR=$TESTSDIR/$TEST/$TEST.err
TESTDIFF=$TESTSDIR/$TEST/$TEST.diff
TESTSCORE=$TESTSDIR/$TEST/$TEST.score
TESTTIME=$TESTSDIR/$TEST/$TEST.time



/bin/rm -f $TESTOUT $TESTERR $TESTDIFF

COMMAND="$LAUNCH $PARAMS $TESTIN"
echo '  ' Running $COMMAND
PWD=`pwd`
cd $BUILDDIR
START=$(date '+%s')
timeout -s SIGKILL $TIMELIMIT $COMMAND > $TEST.out0 2> $TESTERR
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


if test -r $EXPECTED;
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
