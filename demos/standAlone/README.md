This demo shows how to add the code grader to a project, in any programming
language, that is not already packaged to use the gradle or maven build
managers.

* Add the `Grading` directory to the student project. 
* Inside `Grading` supply
    1. the `settings.gradle` and `build.gradle` files as shown,
    2. the gradle wrapper, consisting of the `gradle/` directory and the
       `gradlew*` files.
    3. a `Tests/` directory containing the test suite


For convenience you can provide students with a script to do

    cd Grading; ./gradlew test

or tie that into whatever build manager or script you are already supplying
for the assignment. In this demo, that tie-in appears as the `test` target
within the `makefile`.