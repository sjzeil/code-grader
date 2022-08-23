/*
 * testTerm.cpp
 *
 *  Created on: Apr 15, 2018
 *      Author: zeil
 */

#include "term.h"

#include <string>
#include <vector>
#include <sstream>

#include "unittest.h"

using namespace std;

UnitTest(TermConstructor)
{
	Term t (2, 1);
	assertThat (t.power, is(1));
	assertThat (t.coefficient, is(2));

	Term t2 (2, 2);
	Term t3 (3, 2);

	assertThat(t, is(Term(2,1)));
	assertThat(t, isNot(t2));
	assertThat(t, isNot(t3));

	assertThat (t, isLessThan(t2));
	assertThat (t, isLessThan(t3));

	assertThat (t2, !isLessThan(t));
	assertThat (t2, isLessThan(t3));

	assertThat (t3, !isLessThan(t));
	assertThat (t3, !isLessThan(t2));
}



UnitTest(TermOutput)
{
	Term t (2, 1);
	ostringstream out;
	out << t;
	assertThat(out.str(), is("2x"));

	Term t2(2, 3);
	ostringstream out2;
	out2 << t2;
	assertThat(out2.str(), is("2x^3"));

	Term t3 (3, 0);
	ostringstream out3;
	out3 << t3;
	assertThat(out3.str(), is("3"));

	Term t4 (0, 0);
	ostringstream out4;
	out4 << t4;
	assertThat(out4.str(), is("0"));
}
