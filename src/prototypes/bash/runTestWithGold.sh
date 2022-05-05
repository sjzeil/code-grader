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

mv_existing () {
    if [ -r "$1" ] ;
    then
        mv "$1" "$2"
    fi
}



if [ ! -r $2/$1 ];
then
    echo '***' There is no test in $2/$1
    exit -1
fi

TEST=$1
TESTSDIR="$(absolute_path $2)"
BUILDDIR="$(absolute_path $3)"
GOLDDIR="$(absolute_path $4)"

SCRIPTDIR="$(dirname $(absolute_path $0))"

rm -f $TESTSDIR/$TEST/$TEST.err
rm -f $TESTSDIR/$TEST/$TEST.diff
rm -f $TESTSDIR/$TEST/$TEST.expected
rm -f $TESTSDIR/$TEST/$TEST.time*
$SCRIPTDIR/runTest.sh $TEST $TESTSDIR $GOLDDIR
mv_existing $TESTSDIR/$TEST/$TEST.out $TESTSDIR/$TEST/$TEST.expected
mv_existing $TESTSDIR/$TEST/$TEST.time $TESTSDIR/$TEST/$TEST.timelimit
rm -f $TESTSDIR/$TEST/$TEST.err
rm -f $TESTSDIR/$TEST/$TEST.diff
rm -f $TESTSDIR/$TEST/$TEST.score
$SCRIPTDIR/runTest.sh $TEST $TESTSDIR $BUILDDIR
STATUS=$?
exit $STATUS
