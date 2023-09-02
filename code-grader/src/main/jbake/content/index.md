title=Project Reports: code-grader
date=2022-07-13
type=page
status=published
~~~~~~


# code-grader - Automatic Grading of Programming Assignments 

This project is the long-overdue replacement for the automatic grading script (in Perl) that I have been using for over 25 years.

## Goals

Goals include

* Ease of use
* Support for arbitrary programming languages
* Test oracle (evaluation of pass/fail) based on any of
    * pre-supplied expected output files
    * comparison of output against execution of a "gold version" of the code, supplied by the instructor
    * arbitrary instructor-supplied scripts
* Flexible grading schemes, including partial credit for common mistakes such as upper/lower-case mistakes and misspelled words in boilerplate text.
* Student grade reports written to self-contained HTML files for ease of distribution to students
* Class grade summaries written to CSV files so that instructors can import into grade books. 
* Easy integration with GitHub Classroom or other use of GitHub to support student submissions.

## Status

* In development
* Key features have been prototyped in shell scripts and are being used to grade student programs in a summer 2022 Data Structures and Algorithms course.

# Overview

## Inputs 

There are six critical directories:


1. The _Gold_ directory (optional): Contains the instructor's code for the assignment. 

    This should be a full copy of the program that can be built and run. When provided, this is used to generate the expected test output and expected test run timings.

2. The _Instructor Code_ directory (optional), containing instructor code that must be combined with the student's
   to form the entire program.   If not specified, this defaults to the _Gold_ directory (if that has been provided).

3. The _Submissions_ directory, containing the submissions from all students, each in its own subdirectory. A student's individual submission directory contains all of a student's code, but may also contain copies of files provided to the student by the instructor. 

4. The _Tests_ directory: Contains files that define how the project is to be built and tested. 

5. The _Reporting_ directory: where info about the performance of the students' submissions is placed.

6. The _Staging_ directory (optional): where the student code is collected, compiled, and tested.  By default, this
   is a temporary directory within the _Reporting_ directory.



## The Grading Model

<img src='https://sjzeil.github.io/code-grader/images/flow.png' align='right' style='max-width:66%;' />

1. Setup

    * If the instructor has provided a Gold version, its source code is copied to the gold Stage.
    * If the instructor has provided a Gold version or Instructor code, that code is copied to the student Stage. 
    * A student's submitted code is copied to the student Stage. This may supplement or overwrite the instructor-supplied code. 

2. Build
    * If a Gold version has been provided, the program in the gold Stage is built.
    * The student's program student Stage is built.

3. Test
    
    For each test case in the Test directory:
    * The test case info is copied into the student's Reporting directory, into the student Stage, and, if a gold version was provided, into the gold Stage.
    * If a Gold version was provided, the program in the gold Stage is run on that test case.
    * The program in the student Stage is run on that test case.
        * Student programs that run overly long are terminated.
            * If a Gold version was supplied, the time limit for the student's program is a multiple of the time taken by the Gold program on the same test case. 
            * If no Gold program is available, the time limit can be specified as part of the test case configuration.
    * The output from the student program is scored.
        * By default, output from the student's submission is compared to output from the Gold program on the same test.
        * If no Gold program is available, output from the student's submission is compared to expected output provided as part of the test case info.


4. Report

    Build and test results are assembled into a CSV file for the instructor's gradebook and an HTML grade report
    for each student. 

