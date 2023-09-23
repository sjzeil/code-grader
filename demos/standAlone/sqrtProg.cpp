#include <iostream>
#include <cmath>
#include <cstdio>

using namespace std;

void printSqrt(double x)
{
    double root = sqrt(x) + ((x < 1.0) ? .0002 : 0);  // bug: affects only small roots
    printf("The square root of %.2f is %.4f.\n",
           x, root);
}

int main(int argc, char **argv)
{
    if (argc != 2)
    {
        cerr << "Usage: " << argv[0] << " x" << endl;
        cerr << "  where x is a non-negative number." << endl;
        return 1;
    }
    double x = atof(argv[1]);
    if (x < 0.0)
    {
        cerr << "Number must be non-negative" << endl;
        return 2;
    }
    printSqrt(x);
}
