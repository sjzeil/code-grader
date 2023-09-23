#
#  make all  : compiles the code
#  make      : same as "make all"
#  make test : compiles the code and runs the tests, generates a .html file
#               with the test report
#  make clean: cleans up everything except the source code and the most
#               recent test report
#
all: sqrtProg

sqrtProg: sqrtProg.cpp
	g++ -g -std=c++17 -o $@ $<

test: sqrtProg
	cd Grading; ./gradlew test
	@cp Grading/build/grades/grades/*.html .
	@echo '** ' Grade report is in `ls *.html` ' **'

clean:
	rm -rf sqrtProg Grading/build Grading/*.log Grading/Tests/*/*.err \
	  Grading/Tests/*/*.out Grading/Tests/*/*.message Grading/Tests/*/*.score \
	  Grading/Tests/*/*.time

