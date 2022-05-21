#!/bin/bash
#
#   testAllSubmissions.sh  TestsDirectory SubmissionsDirectory GoldDirectory
#
#  Prototype for automatic grader.  
#
#    Runs testSubmission on each directory within SubmissionsDirectory
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
SUBMISSIONSDIR="$(absolute_path $2)"
GOLDDIR="$(absolute_path $3)"

SubmissionsFiles=`ls $SUBMISSIONSDIR/`
for file in $SubmissionsFiles
do
    if [[ -d $SUBMISSIONSDIR/$file ]];
    then
        $SCRIPTDIR/testSubmission.sh $TESTSDIR  $SUBMISSIONSDIR/$file $GOLDDIR
    fi
done



