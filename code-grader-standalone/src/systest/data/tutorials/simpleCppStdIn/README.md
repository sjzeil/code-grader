# The Assignment

Write a program (in a single `.cpp` file) that reads a number X from standard input (`cin`) and prints a single line of output: 

`The square root of` _X_ `is` _Y_ 

replacing _X_ and _Y_ by the appropriate numbers, printed to 2 and 4 decimal places, respectively.

# Instructor

## Gold version

The instructor has provided a sample _(gold)_ solution in `Gold/instructorSqrt.cpp`

## Tests

The instructor has decided to test each program on the inputs .01, 2, and 100.

In the `Tests/` directory, the instructor creates a subdirectory for each test case.
These are named `test01/`, `test2/`, and `test100/`, respectively.

Because this program reads from the standard input stream, each test case
directory will get a `.in` file containing the input to be given to the program.
These files can have any name so long as they end in `.in`.

For example, in `Tests/test01/`, the instructor places a file `test01.in`
containing a single line:

    0.01


# Students

Five "students" have submitted (in `submissions/`)

* "perfect" has a correct solution to the problem.
* "imperfect" has a formatting error ("the" instead of "The").
* "buggy" produces the correct answer only if the answer is an integer.
* "broken" has syntax errors in the code.
* "looped" has a terribly slow implementation that is likely to get
   caught in an infinite loop.

# Running the Tests

To run the tests, execute the `testMe.sh` script, which contains the command

    java -cp ../../build/libs/code-grader*.jar edu.odu.cs.zeil.codegrader.run.CLI \
        -suite Tests \
        -submissions submissions \
        -gold Gold \
        -isrc - \
        -recording graded

The command parameters indicate that test suite is in `Tests`, the student
submissions are in `submissions/`, the instructor's solution is in `Gold/`,
no instructor code will be combined with student's code before grading,
and the grading information will be recorded in `graded/`.

Because a gold version of the code has been supplied, the instructor did not
need to explicitly provide correct outputs. Instead, for each test case,

1. The gold version will be run on the test case input. The output will be
   captured and the time required to run the program recorded.
2. The student's code will be run on the same test case input, with a time
   limit of 4 times the time taken by hte instructor's code.