#include "utilities.h"

#include <string>

#include "unittest.h"

using namespace std;



UnitTest(testFollower)
{
	assertThat (follower("ABCD"), is("B"));
    assertThat (follower("ZZ"), is("ZZ"));
    assertThat (follower("ZZE"), is("ZZE"));
}

