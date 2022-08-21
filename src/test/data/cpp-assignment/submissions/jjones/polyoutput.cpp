#include "term.h"
#include "polynomial.h"
#include <algorithm>

using namespace std;


std::ostream& operator<< (std::ostream& out, const Polynomial& p)
{
	if (p.degree < 0)
	{
		out << "bad";
	}
	else if (p.degree == 0)
	{
		out << p.coefficients[0];
	}
	else
	{
		bool first_printed = true;
		for (int i = p.degree; i >= 0; --i)
		{
			int c = p.coefficients[i];
			if (c != 0) {
                if (first_printed)
                {
                    if (c != -1)
                        out << Term(c, i);
                    else
                        out << '-' << Term(1, i);
                }
                else
				{
                    if (c < 0)
                        out << " - " << Term(-c, i);
                    else
					    out << " + " << Term(c, i);
				}
				first_printed = false;
				
			}
		}
	}
	return out;
}




