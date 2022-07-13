# code-grader

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

More information will accumulate in the [Wiki](https://github.com/sjzeil/code-grader/wiki).

## Status

* In development
* Key features have been prototyped in shell scripts and are being used to grade student programs in a summer 2022 Data Structures and Algorithms course.

[Project reports](https://sjzeil.github.io/code-grader/)
