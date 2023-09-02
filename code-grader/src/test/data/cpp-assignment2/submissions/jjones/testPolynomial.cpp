/*
 * testPolynomial.cpp
 *
 *  Created on: May 21, 2015
 *      Author: zeil
 */

#include "polynomial.h"

#include <array>
#include <string>
#include <sstream>

#include "unittest.h"


using namespace std;


Polynomial bad;
Polynomial zero(0);
Polynomial xp1 (1, 1); // x+1
Polynomial xm1 (-1, 1); // x-1

int fortyTwo[] = {42};
int parabola[] = {1, -2, 3};  // 3x^2 - 2 x + 1
int degreeTen[] = {1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1}; // x^10 - x^5 + 1


UnitTest (PolynomialDefaultConstructor) {
	assertThat (bad.getDegree(), is(-1));
	assertThat (bad, isEqualTo(Polynomial()));
	assertThat(bad, isNot(zero));

	ostringstream out;
	out << bad;
	assertThat(out.str(), is("bad"));
}

UnitTest (PolynomialLinearConstructor) {
	assertThat (xp1.getDegree(), is(1));
	assertThat(xm1.getCoeff(0), is(-1));
	assertThat(xm1.getCoeff(1), is(1));
	assertThat(xm1.getCoeff(2), is(0));
	
	int oneA = 1;
	assertThat (xp1, isEqualTo(Polynomial(1, oneA)));
	assertThat(xp1, isNot(xm1));
	int oneA3[] = {1, 1, 1};
	assertThat(xp1, isNot(Polynomial(3, oneA3)));

	ostringstream out;
	out << zero;
	assertThat(out.str(), is("0"));

	ostringstream out2;
	out2 << xm1;
	assertThat(out2.str(), is("x - 1"));

}

UnitTest (PolynomialTermConstructor) {
	Term term(2, 3);   // 2 x^3
	Polynomial p(term);
	assertThat (p.getDegree(), is(3));
	assertThat(p.getCoeff(0), is(0));
	assertThat(p.getCoeff(1), is(0));
	assertThat(p.getCoeff(2), is(0));
	assertThat(p.getCoeff(3), is(2));

	int arr[] {0, 0, 0, 2};	
	Polynomial p2(4, arr);
	assertThat(p, isEqualTo(p2));

	Polynomial p3(2,3);
	assertThat(p, isNotEqualTo(p3));

	ostringstream out;
	out << p;
	assertThat(out.str(), is("2x^3"));

}


UnitTest (PolynomialArrayConstructorBasic) {
	int arr[] = {1, -2, 3};
	Polynomial p(3, arr);
	assertThat (p.getDegree(), is(2));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(3));
	assertThat (p.getCoeff(-1), is(0));
	
	assertThat(p, is(Polynomial(3, parabola)));
	int arr2[] = {0, 0, 2};
	assertThat(p, isNot(Polynomial(3, arr2)));

	ostringstream out;
	out << p;
	assertThat(out.str(), is("3x^2 - 2x + 1"));
}


UnitTest (PolynomialArrayConstructorUnNorm) {
	int arr[] = {1, -2, 0};
	Polynomial p(3, arr);
	assertThat (p.getDegree(), is(1));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(0));
	assertThat (p.getCoeff(-1), is(0));

	int p2Coeff[] = {1, -2};
	assertThat(p, is(Polynomial(2, p2Coeff)));
	assertThat(p, isNot(Polynomial(0)));

	ostringstream out;
	out << p;
	assertThat(out.str(), is("-2x + 1"));
}


UnitTest (PolynomialArrayConstructorDegenerate) {
	int arr[] = {0, 0, 0};
	Polynomial p(3, arr);
	assertThat (p.getDegree(), is(0));
	assertThat (p.getCoeff(0), is(0));
	assertThat (p.getCoeff(1), is(0));
	assertThat (p.getCoeff(2), is(0));
	assertThat (p.getCoeff(-1), is(0));
	
	assertThat(p, is(zero));

	ostringstream out;
	out << p;
	assertThat(out.str(), is("0"));
}


UnitTest (PolynomialTermConstructorBasic) {
	Polynomial p(Term(4, 5));
	assertThat (p.getDegree(), is(5));
	assertThat (p.getCoeff(4), is(0));
	assertThat (p.getCoeff(5), is(4));
	assertThat (p.getCoeff(6), is(0));
	assertThat (p.getCoeff(-1), is(0));
	
	int fourFive[] =  {0, 0, 0, 0, 0, 4};
	assertThat(p, is(Polynomial(6, fourFive)));
	assertThat(p, isNot(zero));

	ostringstream out;
	out << p;
	assertThat(out.str(), is("4x^5"));
}



UnitTest (PolynomialCopyBasic) {
	Polynomial p0(3, parabola);
	Polynomial p(p0);
	assertThat (p.getDegree(), is(2));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(3));
	assertThat (p.getCoeff(-1), is(0));
	
	assertThat(p, isEqualTo(p0));

	ostringstream out;
	out << p;
	assertThat(out.str(), is("3x^2 - 2x + 1"));

	p0 *= 2;
	assertThat(p, isNotEqualTo(p0));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(3));
}


UnitTest (PolynomialCopyBad) {
	Polynomial p(bad);
	assertThat (p.getDegree(), is(-1));
	
	assertThat(p, isNotEqualTo(xm1));
	assertThat(p, isNotEqualTo(zero));
	assertThat(p, isEqualTo(bad));

	ostringstream out;
	out << p;
	assertThat(out.str(), is("bad"));
}


UnitTest (PolynomialAssignmentBasic) {
	Polynomial p0(3, parabola);
	Polynomial p (xm1);
	Polynomial p2(p = p0);
	assertThat (p.getDegree(), is(2));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(3));
	assertThat (p.getCoeff(-1), is(0));
	
	assertThat(p, isEqualTo(p0));
	assertThat(p, isEqualTo(p2));


	p0 *= 2;
	assertThat(p, isNot(p0));
	assertThat(p2, isNot(p0));
	assertThat(p, is(p2));
}


UnitTest (PolynomialAssignmentBad) {
	Polynomial p0(3, parabola);
	Polynomial p;
	Polynomial p2(p = p0);  // turns a bad polynomial to good
	assertThat (p.getDegree(), is(2));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(3));
	assertThat (p.getCoeff(-1), is(0));
	
	assertThat(p, isEqualTo(p0));
	assertThat(p, isEqualTo(p2));

	p0 = Polynomial();  // turns a good polynomial to bad
	assertThat(p0.getDegree(), is(-1));
	assertThat(p0, is(bad));
}




UnitTest(PolynomialAdditionBasic) {
	Polynomial p0(3, parabola);
	Polynomial p1(11, degreeTen);

	Polynomial p = p0 + zero;

	assertThat (p.getDegree(), is(2));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(3));
	assertThat (p, is(p0));

	p = p0 + p1;
	assertThat (p, isNot(p0));
	assertThat (p, isNot(p1));
	assertThat (p.getDegree(), is(10));
	assertThat (p.getCoeff(0), is(2));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(3));
	assertThat (p.getCoeff(5), is(-1));
	assertThat (p.getCoeff(10), is(1));
	
	Polynomial q = p1 + p0;
	assertThat (q, is(p));
}

UnitTest(PolynomialAdditionNorm) {
	Polynomial p0(3, parabola);
	int p1Coeff[] = {0, 0, -3};
	Polynomial p1(3, p1Coeff);

	Polynomial p = p0 + p1;

	assertThat (p.getDegree(), is(1));
	assertThat (p.getCoeff(0), is(1));
	assertThat (p.getCoeff(1), is(-2));
	assertThat (p.getCoeff(2), is(0));
	assertThat (p, isNot(p0));
	int p2Coeff[] = {1, -2};
	assertThat (p, is(Polynomial(2, p2Coeff)));
}




UnitTest(PolynomialAdditionDegenerate) {
	Polynomial p0(3, parabola);
	int negParabola[] = {-1, 2, -3};  // -3x^2 + 2 x + -1

	Polynomial p1(3, negParabola);

	Polynomial p = p0 + p1;

	assertThat (p.getDegree(), is(0));
	assertThat (p.getCoeff(0), is(0));
	assertThat (p.getCoeff(1), is(0));
	assertThat (p.getCoeff(2), is(0));
	assertThat (p, is(zero));
}


UnitTest(PolynomialMultiplicationByScalar) {
	Polynomial p0(3, parabola);

	Polynomial p = p0 * -2;

	assertThat (p.getDegree(), is(2));
	assertThat (p.getCoeff(0), is(-2));
	assertThat (p.getCoeff(1), is(4));
	assertThat (p.getCoeff(2), is(-6));

	Polynomial q = -2 * p0;
	assertThat (q, is(p));

	p = p0 * 0;
	assertThat (p.getDegree(), is(0));
	assertThat (p.getCoeff(0), is(0));
	assertThat (p, is(zero));

}

UnitTest(PolynomialMultiplicationByTerm) {
	int arr[] {1, 2, 3};
	Polynomial p0(3, arr);
	Term t(2, 3);
	int arr2[] {0, 0, 0, 2, 4, 6};
	Polynomial expected(6, arr2);

	Polynomial p = p0 * t;
	assertThat (p, is(expected));
}

UnitTest(PolynomialMultiplicationInPlaceByScalar) {
	Polynomial p0(3, parabola);

	Polynomial p = p0;
	p *= -2;

	assertThat (p.getDegree(), is(2));
	assertThat (p.getCoeff(0), is(-2));
	assertThat (p.getCoeff(1), is(4));
	assertThat (p.getCoeff(2), is(-6));

	Polynomial q = -2 * p0;
	assertThat (q, is(p));

	p *= 0;
	assertThat (p.getDegree(), is(0));
	assertThat (p.getCoeff(0), is(0));
	assertThat (p, is(zero));

}

UnitTest(PolynomialDivide) {
	int arr[] = {-1, 0, 0, 1}; // 
	Polynomial p0(4, arr); // x^3-1 == (x-1)(x^2 + x + 1)
	int arr2[] = {1, 1, 1};
	Polynomial factor1 = xm1;
	Polynomial factor2 (3, arr2);

	Polynomial q1 = p0 / factor1;
	assertThat(q1, is(factor2));
	Polynomial q2 = p0 / factor2;
	assertThat (q2, is(factor1));

	Polynomial q3 = p0 / xp1;
	assertThat (q3, is(bad));
}

UnitTest(PolynomialDivide2) {
	int arr[] = {20, -1, -12};
	Polynomial p0(3, arr); // -12x^2 - x + 20 == (3x + 4)(-4x + 5)
	Polynomial factor1 (4, 3);
	Polynomial factor2 (5, -4);

	Polynomial q1 = p0 / factor1;
	assertThat(q1, is(factor2));
	Polynomial q2 = p0 / factor2;
	assertThat (q2, is(factor1));

}



UnitTest(PolynomialDivideByConstants) {
	int arr[] = {-1, 0, 0, 1}; // 
	Polynomial p0(4, arr); // x^3-1 == (x-1)(x^2 + x + 1)
	Polynomial factor1 = xm1;

	Polynomial one (1, 0);
	Polynomial q4 = p0 / one;
	assertThat (q4, is(p0));
	Polynomial q5 = p0 / p0;
	assertThat (q5, is(one));

	Polynomial q6 = zero / factor1;
	assertThat (q6, is(zero));

	Polynomial four (4, 0);
	Polynomial p1 = p0 * 4;
	Polynomial q7 = p1 / four;
	assertThat (q7, is(p0));
	Polynomial q8 = p1 / p0;
	assertThat (q8, is(four));
}
