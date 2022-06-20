#include "student1.h"

#include <string>

std::string clip(const std::string& str)
{
    using namespace std;
    unsigned start = 0;
    while (start < str.size() && str[start] < 'A')
        ++start;
    unsigned stop = str.size() - 1;
    while (stop >= 0 && str[stop] < 'A')
        --stop;
        
    string result = str.substr(start, stop+1);
    return result;
}

