/*
 * readingList.cpp
 *
 *  Created on: May 11, 2021
 *      Author: zeil
 */

#include "readingList.h"
#include <algorithm>

using namespace std;

bool ReadingList::sanityCheck()
{
    Book* b = books;
    int& sz = theSize;
    int& cap = theCapacity;
    if (sz > 0)
        return (sz <= cap) && (get(sz-1) == b[sz-1]);
    else
        return sz <= cap;
}

