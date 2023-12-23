#include "utilities.h"

#include <string>

#include "unittest.h"

using namespace std;



UnitTest(testAllLC)
{
	assertThat (toUC("abcd"), is("ABCD"));
}

UnitTest(testAllUC)
{
	assertThat (toUC("ABCD"), is("ABCD"));
}

UnitTest(testMixedCase)
{
	assertThat (toUC("aBcD"), is("ABCD"));
	assertThat (toUC("Hello World!"), is("HELLO WORLD!"));
}

UnitTest(testSpecial)
{
	assertThat (toUC(""), is(""));
	assertThat (toUC("     "), is("     "));
	assertThat (toUC("12345"), is("12345"));
	
}
