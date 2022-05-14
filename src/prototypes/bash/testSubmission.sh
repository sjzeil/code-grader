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

SCRIPTDIR="$(dirname $(absolute_path $0))"

TESTSDIR="$(absolute_path $1)"
SUBMISSIONDIR="$(absolute_path $2)"
GOLDDIR="$(absolute_path $3)"


/bin/rm -rf $SUBMISSIONDIR/Work $SUBMISSIONDIR/Grading
/bin/cp -rf $TESTSDIR $SUBMISSIONDIR/Grading
/bin/cp -rf $GOLDDIR $SUBMISSIONDIR/Work

TEST=_BOGUS_
buildCommand=getProperty buildCommand
if [[ "$buildCommand" == "" ]];
then
    buildCommand="make compile"
fi
buildWeight=getProperty buildWeight
if [[ "$buildWeight" == "" ]];
then
    buildWeight=1
fi
setupCommand=getProperty setupCommand
if [[ "$setupCommand" == "" ]];
then
    setupCommand="make setup SRC=$SUBMISSIONDIR"
fi

pushd $GOLDDIR
$buildCommand
STATUS=$?
popd
if [[ $STATUS -ne 0 ]];
then
    echo '***' Setup Error: Gold version did not compile.
    exit -1;
fi

pushd $SUBMISSIONDIR/Work
$setupCommand
$buildCommand
STATUS=$?
popd
if [[ $STATUS -eq 0 ]];
then
    buildScore=100
else
    buildScore=0
    echo '***' Submitted code did not compile.
    exit 1;
fi

testScoreSummary=$SUBMISSIONDIR/Grading/summary.csv
echo 'Test,score,weight' > $testScoreSummary
echo "(built successfully),$buildScore,$buildWeight" >> $testScoreSummary
GradingFiles=`ls $SUBMISSIONDIR/Grading`
for file in $GradingFiles
do
    if [[ -d $SUBMISSIONDIR/Grading/$file ]];
    then
        TEST=$file
        pushd $SUBMISSIONDIR/Work
        testWeight=getProperty weight
        if [[ "$testWeight" == "" ]];
        then
            testWeight=1
        fi
        echo $SCRIPTDIR/runTestWithGold.sh $TEST $SUBMISSIONDIR/Grading $SUBMISSIONDIR/Work $GOLDDIR
        $SCRIPTDIR/runTestWithGold.sh $TEST $SUBMISSIONDIR/Grading $SUBMISSIONDIR/Work $GOLDDIR
        if [[ -r $SUBMISSIONDIR/Grading/$TEST/test.score ]];
        then
            score=`cat $SUBMISSIONDIR/Grading/$TEST/test.score`
        else
            score=0
        fi
        echo "$TEST,$score,$testWeight" >> $testScoreSummary
        popd
    fi
done







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

