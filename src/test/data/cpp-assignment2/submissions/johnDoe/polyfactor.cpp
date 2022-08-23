#include "polynomial.h"
#include <cstdlib>
#include <iomanip>
#include <iostream>

using namespace std;


/**
 * Attempt to find a linear factor of a polynomial. 
 * 
 * @param p the polynomial being factored, of degree 2 or higher
 * @param factor output: a linear factor of p or Polynomial() if no factor could be found
 * @param quotient output: the quotient p/factor if a linear factor of p was found, or Polynomial() if 
 *                         no factor could be found
 */
void tryToFactor(const Polynomial& p, Polynomial& factor, Polynomial& quotient)
{
  // If p is divisible by any linear factor ax + b, then a must divide evenly into
  // the highest-degree coefficient of p and b must divide evenly into
  // the lowest-degree coefficient of p.  

  using namespace std::rel_ops; // for operator !=
  
  int highestC = abs(p.getCoeff(p.getDegree()));
  int lowestC =  abs(p.getCoeff(0));
  if (lowestC == 0)
	{
	  factor = Polynomial(0, 1); // x
	  quotient = p / factor;
	  return;
	}
  for (int a = 1; a <= highestC; ++a)
    if (highestC % a == 0)
      for (int b = 1; b <= lowestC; ++b)
        if (lowestC % b == 0) {
          // We'll need to check for various combinations of plus/minus signs.
          factor = Polynomial (b, a);
          quotient = p / factor;
          if (quotient != Polynomial()) {
            return;
          }
          factor = Polynomial(b, -a);
          quotient = p / factor;
          if (quotient != Polynomial()) {
            return;
          }
          factor = Polynomial(-b, a);
          quotient = p / factor;
          if (quotient != Polynomial()) {
            return;
          }
        }
  factor = quotient = Polynomial();
}

/**
 * Prints the linear factors (ax+b where a and b are integers) of 
 * a polynomial.
 * 
 * @param p a polynomial
 * @return true if p has at least one linear factor
 */
void factor (Polynomial p)
{
  while (p.getDegree() > 1)
  {
      Polynomial factor;
      Polynomial quotient;
      tryToFactor (p, factor, quotient);
      if (factor == Polynomial() || quotient == Polynomial())
      {
        break;
      }
      else
      {
        cout << "factor: " << factor  << endl;
        p = quotient;
      }
  }
  if (p.getDegree() < 0)
  {
      cout << "could not factor: " << p << endl;
  }
  else if (p.getDegree() <= 1)
  {
    cout << "factor: " << p << endl;
  }
  else {
    cout << "could not factor: " << p << endl;
  }
}

/**
 * Run as 
 *   ./polyfactor cn cn-1 ... c1 c0
 * where n is the degree of the polynomial and the c_i are integer coefficients
 * defining a polynomial c0 + c1 * x + c2 * x^2 + ... + cn * x^n
 * 
 */
int main(int argc, char **argv)
{
  if (argc == 1)
	{
	  cerr << "Usage: ./" << argv[0] << " cn ... c1 c0" << endl;
	  return 1;
	}
  int degree = argc - 1;
  int* coefficients = new int[degree];
  for (int i = 0; i < degree; ++i)
     coefficients[degree-i-1] = atoi(argv[i+1]);

  Polynomial poly (degree, coefficients);
  delete[] coefficients;
  
  cout << "Attempting to factor: " << poly << endl;
  factor(poly);
  
}
