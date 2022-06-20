#include "instructor1.h"
#include "student1.h"

#include <string>
#include <iostream>

using namespace std;

int main (int argc, char** argv)
{
    string s1 = argv[argc-1];
    print (s1);
    string s2;
    cin >> s2;
    print (s2);
    return 0;
}

void print (string s)
{
    cout << s << endl;
}

