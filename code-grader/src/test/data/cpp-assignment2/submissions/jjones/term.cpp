#include "term.h"
#include <iostream>

using namespace std;


std::ostream& operator<< (std::ostream& out, const Term& t)
{
	if (t.power == 0)
		out << t.coefficient;
	else
	{
		if (t.coefficient != 1)
			out << t.coefficient;
		out << "x";
		if (t.power > 1)
			out << "^" << t.power;
	}
	return out;
}

bool operator== (const Term& left, const Term& right)
{
	return left.power == right.power && left.coefficient == right.coefficient;
}

bool operator< (const Term& left, const Term& right)
{
	if (left.power == right.power)
		return left.coefficient < right.coefficient;
	else
		return left.power < right.power;
}
