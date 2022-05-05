#!/bin/bash
#
#   testSubmission.sh  TestsDirectory SubmissionDirectory GoldDirectory
#
#  Prototype for automatic grader.  
#
#    1) Copies TestsDirectory to SubmissionDirectory/Grading
#    2) Copies Gold project to SubmissionDirectory/Work
#    3) Copies selected submitted files to SubmissionDirectory/Work ("make submit" in Work)
#    4) Builds Gold project
#    5) Builds SubmissionDirectory/Work
#    6) For each test case:
#       7) run test with Gold.  .out => .expected  .time => .timeLimit  remove .err .score
#       8) run test with Work.
#    9) Collect scores into SubmissionDirectory  
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
        if [ -r $TESTSDIR/Defaults/Defaults.yaml ];
        then
            TMP=`grep -i '^ *'$1':' $TESTSDIR/Defaults/Defaults.yaml | sed 's/^[^:]*: *//'`
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


