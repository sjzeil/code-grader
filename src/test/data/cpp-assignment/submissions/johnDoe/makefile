MAINPROG=polyfactor
DIR=${PWD}
ASST=$(notdir ${DIR})
CC=gcc
CXX=g++
##
# Adjust settings for different compilers
#
ifeq ($(OS),Windows_NT)
#
# Flags for Windows compilers
CPPFLAGS=-g -std=c++17 -MMD -pthread -D_GLIBCXX_DEBUG -Wall
LFLAGS=
RM=del /q
EXE=.exe
else
#
# Flags for Linux & MacOS
 CPPFLAGS=-g -std=c++17 -fsanitize=address -MMD -pthread -D_GLIBCXX_DEBUG -Wall
# CPPFLAGS=-g -std=c++17 -MMD -pthread -D_GLIBCXX_DEBUG -Wall
LFLAGSx=
RM=/bin/rm -rf
EXE=
endif
#
########################################################################
# Macro definitions for "standard" C and C++ compilations
#
CFLAGS=-g
TARGET=$(MAINPROG)$(EXE)
CPPS=$(wildcard *.cpp)
MAINCPPS=$(filter-out unittest.cpp test%.cpp, $(CPPS))
TESTCPPS=$(filter-out $(MAINPROG).cpp, $(CPPS))
 
LINK=g++ $(CPPFLAGS)
#
########################################################################
#
#
#  In most cases, you should not change anything below this line.
#
#  The following is "boilerplate" to set up the standard compilation
#  commands:
#


MAINOBJS=$(MAINCPPS:%.cpp=%.o)
TESTOBJS=$(TESTCPPS:%.cpp=%.o)
DEPENDENCIES = $(CPPS:%.cpp=%.d)

# Macro definitions for systems tests
#
TESTSFILES=$(wildcard Tests/*/*)
TESTSDIRS=$(sort $(dir $(TESTSFILES)))
TESTSOUT=$(addsuffix test.out, $(TESTSDIRS))



%.d: %.cpp
	touch $@

%.o: %.cpp
	$(CXX) $(CPPFLAGS) -o $@ -c $*.cpp

# 
# Targets:
# 

all: makefile

compile: $(TARGET) unittest$(EXE) 

tests: unitTests systemTests

unitTests: unitTests.date

unitTests.date: unittest$(EXE) 
	date > unitTests.date
	./unittest$(EXE)

systemTests: $(TESTSOUT)

Tests/%/test.out: $(TARGET)
	bash ./runTest.sh $* Tests .

$(TARGET): $(MAINOBJS)
	$(LINK) $(FLAGS) -o $(TARGET) $^ $(LFLAGS)


unittest$(EXE): $(TESTOBJS)
	$(LINK) $(FLAGS) -o $@ $^ $(LFLAGS)


clean: cleanTests
	-/bin/rm -rf *.d *.o $(TARGET) unittest$(EXE) docs 

cleanTests:
	-/bin/rm -rf Tests/*/*.out Tests/*/*.err Tests/*/*.diff Tests/*/*.score Tests/*/*.time unitTests.date

documentation:
	-mkdir docs
	doxygen Doxyfile




make.dep: $(DEPENDENCIES)
	-cat $(DEPENDENCIES) > $@

include make.dep
