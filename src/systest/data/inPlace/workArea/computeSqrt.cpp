#include <iostream>
#include <iomanip>
#include <cmath>

using namespace std;

int main()
{
    double x;
    cin >> x;
    double y = sqrt(x);
    cout << "The square root of " << setprecision(2) << x << " is "
         << (int)y << endl;
    return 0;
}