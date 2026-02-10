#include "unittest.h"
#include "readingList.h"
#include <algorithm>
#include <sstream>
#include <vector>
#include <list>
#include <set>
#include <array>

using namespace std;

struct Setup {
    Book b1;
    Book b2;
    Book b3;

    Setup()
    {
        b1 = Book("etext13", "Carroll, Lewis", "The Hunting of the Snark");
        b2 = Book("etext11792", "Poe, Edgar Allan", "The Raven");
        b3 = Book("etext9038", "Twain, Mark", "The Adventures of Tom Sawyer");
    }
};

UnitTest(testRLDefaultConstructor)
{
    ReadingList rl;
    assertThat (rl.size(), isEqualTo(0));
    assertThat (rl.capacity(), isGreaterThan(0));
    assertThat (rl, isEqualTo(ReadingList()));
    assertTrue (rl.sanityCheck());
}

UnitTest(testRLConstructor)
{
    ReadingList rl(5);
    assertThat (rl.size(), isEqualTo(0));
    assertThat (rl.capacity(), isGreaterThan(4));
    assertThat (rl, isEqualTo(ReadingList()));
    assertTrue (rl.sanityCheck());
}


UnitTest(testRLAddDiscrete)
{
    Setup s;
    ReadingList rl(5);
    rl.add(s.b1);
    rl.add(s.b2);
    rl.add(s.b3);

    assertThat (rl.size(), isEqualTo(3));
    assertThat (rl.capacity(), isGreaterThan(2));
    assertThat (rl, isNotEqualTo(ReadingList()));

    assertTrue(rl.contains(s.b1.getID()));
    assertTrue(rl.contains(s.b2.getID()));
    assertTrue(rl.contains(s.b3.getID()));
    assertFalse(rl.contains(s.b1.getID() + "x"));

    assertThat(rl.find(s.b1.getID()), isEqualTo(1));
    assertThat(rl.find(s.b2.getID()), isEqualTo(0));
    assertThat(rl.find(s.b3.getID()), isEqualTo(2));
    assertThat(rl.find(s.b3.getID()+"0"), isEqualTo(-1));

    assertThat(rl.get(0), isEqualTo(s.b2));
    assertThat(rl.get(1), isEqualTo(s.b1));
    assertThat(rl.get(2), isEqualTo(s.b3));

    ReadingList rl2;
    rl2.add(s.b3);
    rl2.add(s.b2);
    rl2.add(s.b1);

    assertThat(rl, isEqualTo(rl2));
    
    assertTrue (rl.sanityCheck());

}


UnitTest(testRLAddDuplicates)
{
    Setup s;
    ReadingList rl(8);
    rl.add(s.b3);
    rl.add(s.b2);
    rl.add(s.b1);
    rl.add(s.b2);

    assertThat (rl.size(), isEqualTo(3));
    assertThat (rl.capacity(), isGreaterThan(2));
    assertThat (rl, isNotEqualTo(ReadingList()));

    assertTrue(rl.contains(s.b1.getID()));
    assertTrue(rl.contains(s.b2.getID()));
    assertTrue(rl.contains(s.b3.getID()));
    assertFalse(rl.contains(s.b1.getID() + "x"));

    assertThat(rl.find(s.b1.getID()), isEqualTo(1));
    assertThat(rl.find(s.b2.getID()), isEqualTo(0));
    assertThat(rl.find(s.b3.getID()), isEqualTo(2));
    assertThat(rl.find(s.b3.getID()+"0"), isEqualTo(-1));

    assertThat(rl.get(0), isEqualTo(s.b2));
    assertThat(rl.get(1), isEqualTo(s.b1));
    assertThat(rl.get(2), isEqualTo(s.b3));

    assertTrue (rl.sanityCheck());
}

UnitTest(testRLRemove)
{
    Setup s;
    ReadingList rl(10);
    rl.add(s.b1);
    rl.add(s.b2);
    rl.add(s.b3);
    rl.remove(s.b1.getID());

    assertThat (rl.size(), isEqualTo(2));
    assertThat (rl.capacity(), isGreaterThan(1));
    assertThat (rl, isNotEqualTo(ReadingList()));

    assertFalse(rl.contains(s.b1.getID()));
    assertTrue(rl.contains(s.b2.getID()));
    assertTrue(rl.contains(s.b3.getID()));
    assertFalse(rl.contains(s.b1.getID() + "x"));

    assertThat(rl.find(s.b1.getID()), isEqualTo(-1));
    assertThat(rl.find(s.b2.getID()), isEqualTo(0));
    assertThat(rl.find(s.b3.getID()), isEqualTo(1));
    assertThat(rl.find(s.b3.getID()+"0"), isEqualTo(-1));

    assertThat(rl.get(0), isEqualTo(s.b2));
    assertThat(rl.get(1), isEqualTo(s.b3));

    assertTrue (rl.sanityCheck());
}


UnitTest(testRLAssign)
{
    Setup s;
    ReadingList rl(10);
    rl.add(s.b1);
    rl.add(s.b2);


    ReadingList rl2;
    ReadingList rl3(rl2 = rl);

    assertThat(rl2, isEqualTo(rl));
    assertThat(rl3, isEqualTo(rl));
    
    assertThat(rl2.size(), isEqualTo(2));
    assertThat (rl2.capacity(), isGreaterThan(1));
    assertThat (rl2, isNotEqualTo(ReadingList()));

    assertThat(rl2.get(0), isEqualTo(s.b2));
    assertThat(rl2.get(1), isEqualTo(s.b1));

    rl2.add(s.b3);

    assertThat(rl2, isNotEqualTo(rl));
    assertThat(rl3, isEqualTo(rl));
    
    assertThat(rl.size(), isEqualTo(2));
    assertThat(rl2.size(), isEqualTo(3));
    assertThat(rl3.size(), isEqualTo(2));
    
    assertThat (rl2.capacity(), isGreaterThan(1));
    assertThat (rl2, isNotEqualTo(ReadingList()));
}


UnitTest(testRLCopy)
{
    Setup s;
    ReadingList rl(10);
    rl.add(s.b1);
    rl.add(s.b2);


    ReadingList rl2 (rl);

    assertThat(rl2, isEqualTo(rl));
    
    assertThat(rl2.size(), isEqualTo(2));
    assertThat (rl2.capacity(), isGreaterThan(1));

    assertThat(rl2.get(0), isEqualTo(s.b2));
    assertThat(rl2.get(1), isEqualTo(s.b1));

    rl2.add(s.b3);

    assertThat(rl2, isNotEqualTo(rl));
    
    assertThat(rl.size(), isEqualTo(2));
    assertThat(rl2.size(), isEqualTo(3));
    
}




UnitTest(testRLInput)
{
    ReadingList rl(1);

    string instring = string()
        + "etext29662\tLeiber, Fritz, 1910-1992\tThe Moon is Green\n"
        + "etext1195\tJames, Henry, 1843-1916\tGlasses\n"
        + "etext14784\tWade, Mary Hazelton Blanchard, 1860-1936\tTimid Hare\n";

    istringstream in(instring);
    in >> rl;

    assertThat (rl.size(), isEqualTo(3));
    assertThat (rl.capacity(), isGreaterThan(2));

    assertTrue(rl.contains("etext1195"));
    assertTrue(rl.contains("etext14784"));
    
    assertThat(rl.get(rl.find("etext1195")).getAuthor(), isEqualTo("James, Henry, 1843-1916"));
    assertThat(rl.get(rl.find("etext1195")).getTitle(), isEqualTo("Glasses"));
}






UnitTest (zzMemoryCheck) {
    int leaked = Counted::getCurrentCount(); 
    if (leaked > -1) {
        cerr << "Leaked memory: " << leaked << " blocks" << endl;
    }
    assertThat (leaked, isEqualTo(0));
}

