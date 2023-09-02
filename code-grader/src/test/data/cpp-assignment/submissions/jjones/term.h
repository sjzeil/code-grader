#ifndef TERM_H
#define TERM_H

#include <iostream>

/**
 * A single term of a polynomial,
 * denoting coefficient * x ^ power
 */
struct Term
{
	int coefficient;
	int power;

	Term (int coeff, int pow) : coefficient(coeff), power(pow) {}
};

std::ostream& operator<< (std::ostream& out, const Term& t);
bool operator== (const Term& left, const Term& right);
bool operator< (const Term& left, const Term& right);

#endif
