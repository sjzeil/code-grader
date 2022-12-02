#include <iostream>
#include <cmath>
#include <cstdlib>


using namespace std;

int main()
{
    double x;
    cin >> x;

    double y = 0.0;
    while (y*y != x) {
        y = (double)(rand() % 10000);
        y = y / 1000.0;
    }
    cout << "The square root of " << setprecision(2) << x << " is "
         << (int)y << endl;
    return 0;
}
