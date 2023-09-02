#include "utilities.h"

#include <string>

#include "unittest.h"

using namespace std;



UnitTest(testSeparatorDistinct)
{
	assertThat (findSeparator("ABCD", "EFGH"), is("B"));
}

UnitTest(testSeparatorSharedPrefixes)
{
	assertThat (findSeparator("ABCD", "ABXY"), is("ABD"));
}

UnitTest(testSeparatorZs)
{
	assertThat (findSeparator("ABZ", "ABZA"), is("ABZ"));
}

