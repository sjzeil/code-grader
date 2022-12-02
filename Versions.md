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
