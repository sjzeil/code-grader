title=Project Documentation: code-grader
date=2020-04-01
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
* Student grade reports written to spreadsheets, so that  instructors can compute overall scores in any way that they desire.
* Easy integration with GitHub Classroom or other use of GitHub to support student submissions.

## Status

* In development
* Key features have been prototyped in shell scripts and are being used to grade student programs in a summer 2022 Data Structures and Algorithms course.

# Overview

## Inputs 

There are five critical file directories:


1. The _Gold_ directory (optional): Contains the instructor's code for the assignment. 

    At the very least, this includes files provided by the instructor that students might not be expected to change. 

    In many cases, this contains an entire working solution to the assignment.

2. The _Submissions_ directory, containing the submissions from all students, each in its own subdirectory. A student's individual submission directory contains all of a student's code, but may also contain copies of files from the _gold_ directory provided by the instructor. 

3. The _Tests_ directory: Contains files that define how the project is to be built and tested. 

4. The _Staging_ directory: where the student code is collected, compiled, and tested.

5. The _Reporting_ directory: where info about the performance of the students' submissions is placed.

## The Grading Model

1. Setup

    The contents of the Release directory, excluding selected files, are copied to the Submission directory.

2. Build
    1. If a Gold directory has been provided, the program in it is built.
    2. The student's program in the Submission directory is built.

3. Tests
    
    Each test case in the Test directory is run and scored.

4. Reporting

    Build and test results are assembled into a spreadsheet, which
    is deposited in the Submission directory
