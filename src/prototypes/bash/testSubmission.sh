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

#
#  Collect configuration properties from $TESTSDIR/Defaults/config.yaml and $TESTSDIR/$TEST/config.yaml
#

getFileByExtension () { # dir extension
    Candidates=($1/*.$2)
    if [ -e ${Candidates[0]-} ];
    then
        echo ${Candidates[0]}
    else
        echo
    fi
}


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


SCRIPTDIR="$(dirname $(absolute_path $0))"

TESTSDIR="$(absolute_path $1)"
SUBMISSIONDIR="$(absolute_path $2)"
GOLDDIR="$(absolute_path $3)"

AsstDir=`dirname $GOLDDIR`
AsstName=`basename $AsstDir`
SubmitterName=`basename $SUBMISSIONDIR`

/bin/rm -rf $SUBMISSIONDIR/Work $SUBMISSIONDIR/Grading
/bin/cp -rf $TESTSDIR $SUBMISSIONDIR/Grading
/bin/cp -rf $GOLDDIR $SUBMISSIONDIR/Work

TEST=_BOGUS_
buildCommand=$(getProperty buildCommand)
if [[ "$buildCommand" == "" ]];
then
    buildCommand="make compile"
fi
buildWeight=$(getProperty buildWeight)
if [[ "$buildWeight" == "" ]];
then
    buildWeight=1
fi
setupCommand=$(getProperty setupCommand)
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
if [[ $STATUS -eq 0 ]];
then
    buildScore=100
    echo > build.msg
else
    buildScore=0
    echo '***' Submitted code did not compile.
    $buildCommand 2>&1  | tr '"' "'" > build.msg
fi
popd

testScoreSummary=$SUBMISSIONDIR/Grading/summary.csv
testInfoSummary=$SUBMISSIONDIR/Grading/testInfo.csv
buildMsg=`cat $SUBMISSIONDIR/Work/build.msg`
echo "assignment name,$AsstName" >> $testInfoSummary
echo "submitted by,$SubmitterName" >> $testInfoSummary
echo "built successfully?,$buildScore" >> $testInfoSummary
echo "build weight,$buildWeight" >> $testInfoSummary
echo "build message,\"$buildMsg\"" >> $testInfoSummary

pushd $SUBMISSIONDIR/Grading
commitDate=`git log -1 --date=local --format=%cd origin/main`
popd
echo "last commit,$commitDate" >> $testInfoSummary



echo 'Test,score,weight' > $testScoreSummary
GradingFiles=`ls $SUBMISSIONDIR/Grading`
for file in $GradingFiles
do
    if [[ -d $SUBMISSIONDIR/Grading/$file ]];
    then
        TEST=$file
        pushd $SUBMISSIONDIR/Work
        testWeight=$(getProperty weight)
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

GradeSheetTemplate=$(getFileByExtension $TESTSDIR/.. xlsx)
if [[ "$GradeSheetTemplate" != "" ]];
then
    cp $GradeSheetTemplate $SUBMISSIONDIR/Grading/$SubmitterName.xlsx
    /home/zeil/bin/insertCSV.sh $SUBMISSIONDIR/Grading/$SubmitterName.xlsx info $testInfoSummary
    /home/zeil/bin/insertCSV.sh $SUBMISSIONDIR/Grading/$SubmitterName.xlsx tests $testScoreSummary
fi





