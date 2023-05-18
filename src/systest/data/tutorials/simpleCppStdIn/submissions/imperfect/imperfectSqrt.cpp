#include <iostream>
#include <iomanip>
#include <cmath>

using namespace std;

int main()
{
    double x;
    cin >> x;
    double y = sqrt(x);
    cout.setf(ios::fixed, ios::floatfield);
    cout << "the square root of " << setprecision(2) << x << " is "
        << setprecision(4) << y << endl;
    return 0;
}