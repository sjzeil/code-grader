#include "polynomial.h"
#include <algorithm>

using namespace std;


Polynomial::Polynomial ()
: degree(-1), coefficients(nullptr)
{
}

Polynomial::Polynomial (int b, int a)
: degree(1), coefficients(new int[2])
{
	coefficients[0] = b;
	coefficients[1] = a;
	normalize();
}

Polynomial::Polynomial (Term term)
: degree(term.power), coefficients(new int[term.power+1])
{
	for (int i = 0; i < degree; ++i)
	    coefficients[i] = 0;
	coefficients[degree] = term.coefficient;
	normalize();
}


Polynomial::Polynomial (int nC, int coeff[])
: degree(nC-1), coefficients(new int[nC])
{
	for (int i = 0; i <= degree; ++i)
		coefficients[i] = coeff[i];
	normalize();
}



void Polynomial::normalize ()
{
	while (degree+1 > 1 && coefficients[degree] == 0)
		--degree;
}


int Polynomial::getDegree() const
{
	return degree;
}

int Polynomial::getCoeff(int power) const
{
	if (power >= 0 && power <= degree)
	{
		return coefficients[power];
	}
	else
	{
		return 0.0;
	}
}

Polynomial Polynomial::operator+ (const Polynomial& p) const
{
	if (degree == -1 || p.degree == -1)
		return Polynomial();

	int resultSize = max(degree+1, p.degree+1);
	int* resultCoefficients = new int[resultSize];
	int k = 0;
	while (k <= getDegree() && k <= p.getDegree())
	{
		resultCoefficients[k] = coefficients[k] + p.coefficients[k];
		++k;
	}

	for (int i = k; i <= getDegree(); ++i)
	    resultCoefficients[i] = coefficients[i];

	for (int i = k; i <= p.getDegree(); ++i)
	    resultCoefficients[i] = p.coefficients[i];


	Polynomial result(resultSize, resultCoefficients);
	delete[] resultCoefficients;

	return result;
}


Polynomial Polynomial::operator* (int scale) const
{
	if (degree == -1)
		return Polynomial();

	Polynomial result (*this);
	for (int i = 0; i <= degree; ++i)
		result.coefficients[i] = scale * coefficients[i];
	result.normalize();
	return result;
}

Polynomial Polynomial::operator* (Term term) const
{
	if (degree == -1)
		return Polynomial();

	int* results = new int[degree + 1 + term.power];

	for (int i = 0; i < term.power; ++i)
		results[i] = 0;

	for (int i = 0; i < degree + 1; ++i)
		results[i+term.power] = coefficients[i] * term.coefficient;

	Polynomial result (degree + 1 + term.power, results);
	delete [] results;
	return result;
}


void Polynomial::operator*= (int scale)
{
	if (degree == -1)
		return;
	for (int i = 0; i <= degree; ++i)
		coefficients[i] = scale * coefficients[i];
	normalize();
}

Polynomial Polynomial::operator/ (const Polynomial& denominator) const
{
	if (degree == -1 || denominator.degree == -1)
		return Polynomial();
	if (*this == Polynomial(0))
		return *this;
	if (denominator.getDegree() > getDegree())
		return Polynomial();


	int resultSize = degree - denominator.degree + 1;
	int* results = new int[resultSize];
	for (int i = 0; i < resultSize; ++i)
		results[i] = 0;

	Polynomial remainder = *this;
	for (int i = resultSize-1; i >= 0; --i)
	{
		// Try to divide remainder by denominator*x^(i-1)
		int remainder1stCoeff = remainder.getCoeff(i+denominator.getDegree());
		int denominator1stCoeff = denominator.getCoeff(denominator.getDegree());
		if (remainder1stCoeff % denominator1stCoeff == 0) {
			results[i] = remainder1stCoeff / denominator1stCoeff;
			Polynomial subtractor = denominator * Term(-results[i], i);
			remainder = remainder + subtractor;
		} else {
			// Can't divide this
			break;
		}
	}
	if (remainder == Polynomial(0)) {
		Polynomial result (resultSize, results);
		delete [] results;
		return result;
	}
	else
	{
		// A non-zero remainder could not be removed - division fails
		delete [] results;
		return Polynomial();
	}
}

bool Polynomial::operator== (const Polynomial& right) const
{
	if (degree != right.degree)
		return false;
	for (int i=0;i<degree+1;++i)
		if (!(coefficients[i] == right.coefficients[i]))
			return false;
	return true;
}

/*
bool Polynomial::operator== (const Polynomial& right) const // Method 1: Creating operator== for pointer *this
{
	if (degree != right.getDegree())
		return false;
	for (int i=0;i<degree;i++)
		if (!(coefficients[i] == right.getCoeff(i)))
			return false;
	return true;
}



Polynomial Polynomial::deepCopyOf(const Polynomial& b) // Method 2: Creating a deep copy function of Polynomial
{
	Polynomial copy;
	copy.degree = b.degree;
	for (int i=0;i<degree;++i)
		copy.coefficients[i] = b.coefficients[i];
	return copy;
}

Polynomial& Polynomial::operator= (const Polynomial& b) // Method 3: Creating a shallow copy function of Polynomial
{
	degree = b.degree;
	coefficients = b.coefficients;
	return *this;
}*/

