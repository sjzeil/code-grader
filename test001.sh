#!/bin/sh
mkdir build
mkdir build/test001
cp -rf src/test/assignment build/test001/
bash -x src/prototypes/bash/testSubmission.sh build/test001/assignment/tests  build/test001/assignment/submissions/good  build/test001/assignment/gold
