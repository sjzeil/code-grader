#include <iostream>
#include <string>
#include <fstream>

#include "book.h"
#include "readingList.h"

using namespace std;



ReadingList readList(const string& fileName)
{
	ReadingList c;
	ifstream in (fileName);
	in >> c;
	in.close();
	return c;
}


ReadingList mergeReadingLists (const ReadingList& cat1, const ReadingList& cat2)
{
	ReadingList result (cat1.size() + cat2.size());
	int i = 0;
	int j = 0;
	while (i < cat1.size() && j < cat2.size())
	{
		Book b1 = cat1.get(i);
		Book b2 = cat2.get(j);
		if (b1.getID() < b2.getID())
		{
			result.add(b1);
			++i;
		}
		else
		{
			result.add(b2);
			++j;
		}
	}
	while (i < cat1.size())
	{
		result.add(cat1.get(i));
		++i;
	}
	while (j < cat2.size())
	{
		result.add(cat2.get(j));
		++j;
	}
	return result;
}


void mergeReadingListFiles (const string& readingListFile1, const string& readingListFile2)
{
	ReadingList c1, c2;
	c1 = readList(readingListFile1);
    cout << readingListFile1 << " contained " << c1.size() << " books." << endl;
	c2 = readList(readingListFile2);
    cout << readingListFile2 << " contained " << c2.size() << " books." << endl;
    ReadingList c3 = mergeReadingLists (c1, c2);
    cout <<  "Their merge contains " << c3.size() << " books." << endl;
    cout << c3 << flush;
}

int main (int argc, char** argv)
{
	if (argc != 3)
	{
		cerr << "Usage: " << argv[0] <<
				"readingListFile1 readingListFile2" << endl;
		return -1;
	}
	string file1 = argv[1];
	string file2 = argv[2];
	mergeReadingListFiles (file1, file2);
	if (Counted::getCurrentCount() == 0)
	{
		cerr << "No memory leak detected." << endl;
		return 0;
	}
	else
	{
		cerr << "Memory leak detected: " << Counted::getCurrentCount() << endl;
		return -2;
	}
}
