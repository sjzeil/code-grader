#
#  make all  : compiles the code
#  make      : same as "make all"
#  make test : compiles the code and runs the tests, generates a .html file
#               with the test report
#  make clean: cleans up everything except the source code and the most
#               recent test report
#
all: sqrtProg.class

sqrtProg.class: sqrtProg.java
	javac $<

test: sqrtProg.class
	cd Grading; ./gradlew test

clean:
	rm -r *.class Grading/build Grading/*.log Grading/Tests/*/*.err \
	  Grading/Tests/*/*.out Grading/Tests/*/*.message Grading/Tests/*/*.score \
	  Grading/Tests/*/*.time

