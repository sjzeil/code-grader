1.2.7 11/4/2025
* Testcase messages will no longer be clipped at 5000 characters if they contain
  an HTML passthrough marker.  Such clipping is prone to generating broken HTML.

1.2.6 08/18/2025
* If unable to determine submission date, continue with just a warning

1.2.5 12/30/2024
* If std err is non-empty after running a test case, the contents are appended to the report even if the test case settings indicate that std err is not checked.

    This should make it more obvious when student code has crashed.

1.2.4 11/10/2024
* Fixed bug in which repository logs were not being parsed on Windows systems
  (determining when a repo was last pushed/submitted).

1.2.3 10/09/2024
* Submission dates are now determined before running test cases, in case any
  test case modifies files used to determine the submission date.

1.2.2 9/5/2024
* Fixed bug causing git repository dates to be reported in UTC instead of
  in local time.

1.2.1 5/11/2024
* junit5 oracle now locates test reports in Gradle and Maven builds if it cannot
  determine tests passed from the standard output.

1.2.0 5/10/2024
* Added new boolean test case option, "multiplier". A multiplier case does not
  add its score to the total but multiplies it. For example, a score of 50
  (out of 100) reduces the total of all other test cases by half. 

    This is intended to permit test cases that check for assignment requirements,
    e.g., an assignment that requires that students use java.util.HashMap might
    have a test case that uses Java reflection to make sure that at least one
    data member has that type, dropping the total score to zero if students
    submit an array-based solution.

    Another use might be for "modify this program"-style assignments to test
    whether students are simply resubmitting the original unchanged code.

1.1.9 4/30/2024
* Fixed bug affecting Windows use. Command lines for launching tests were being
  echoed, captured as part of the output, and confusing oracles into issuing
  false failure reports.

1.1.8 2/20/2024
* Improved handling of programs with very large outputs (up to 250,000 chars)
* Added new handling of student named '-'.  This student name signals that
  the code to be graded is directly in the submissions folder rather than in
  a student-named sub-directory of the submissions folder.  This will replace
  the -inPlace processing option soon. That option is now deprecated.

1.1.7 2/16/2024
* Fixed bug causing test cases to have shorter time limits when run a second time

1.1.6 12/19/2023
* Added a .txt version of the student grade report.

1.1.5 9/23/2023
* Fixed but causing test command line params to be duplicated when listed
  explicitly within the launch command.

1.1.4 12/2/2022
* Commands lines and parameters for test execution may now contain wild cards.
* Improved handling of process killing when timing out long-running tests in
  Linux. Should be less likely to leave zombie processes and `.nfs` files
  behind.

1.1.3 10/26/2022
* The stage directory for a student's submission is now named with the 
  student's ID instead of the generic name `submission`. This reduces
  the potential for conflicts if the grader should be run simultaneously
  for two different students.

1.1.2 10/11/2022

* Directories starting with "`.`" or "`__`" are not considered to be test case
  directories. (These can be created inadvertantly when running some tests,
  e.g., a test of a Python program or that uses a Python oracle script
  may generate a `__pycache__` directory.)

1.1.1 10/4/2022

* Fixed bug where stderr was not collected when explicitly requested
  as part of a test case's properties.

1.1.0 9/25/2022  

* Added oracles for JUnit tests, TAP (Test Anything Protocol) tests, and
  for programs that self-grade.
* Added options for determining the date-time of a submission by examining the
  submitted files.  (1.0.0 only used git repo commit dates.)

1.0.0  9/17/2022   Base release.
