/*
 * readingList.cpp
 *
 *  Created on: May 11, 2021
 *      Author: zeil
 */

#include "readingList.h"
#include <algorithm>

using namespace std;

/**
 * A list of books, identified by Gutenberg ID.
 */
/**
 * Create a new empty reading list, capable of holding up to
 * maxCapacity books.
 *
 * @param maxCapacity maximum number of books this reading list
 *    can hold.
 */
ReadingList::ReadingList(int maxCapacity)
{
	theCapacity = maxCapacity;
	theSize = 0;
	books = new Book[theCapacity];
}


ReadingList::ReadingList(const ReadingList &c)
{
	theCapacity = c.theSize;
	theSize = c.theSize;
	books = new Book[theCapacity];
	for (int i = 0; i < theSize; ++i)
	{
		books[i] = c.books[i];
	}
}

ReadingList &ReadingList::operator=(const ReadingList &c)
{
	if (this != &c)
	{
		delete[] books;
		theCapacity = c.theSize;
		theSize = c.theSize;
		books = new Book[theCapacity];
		for (int i = 0; i < theSize; ++i)
		{
			books[i] = c.books[i];
		}
	}
	return *this;
}

ReadingList::~ReadingList()
{
	delete[] books;
}

/**
 * How many books currently in this reading list?
 *
 * @return number of books in the reading list.
 */
int ReadingList::size() const
{
	return theSize;
}

/**
 * How many books can this reading list hold?
 *
 * @return max capacity of this reading list.
 */
int ReadingList::capacity() const
{
	return theCapacity;
}

/**
 * Attempt to alter the capacity of this reading list so that
 * it can hold at least newCapcity books.  Existing books
 * in the reading list are retained.
 *
 * @param newCapacity maximum number of books we would like to be able
 *    to store in this reading list.
 * @post capacity() >= newCapacity
 */
void ReadingList::reserve(int newCapacity)
{
	if (newCapacity >= theSize)
	{
		// Allocate a new array for the books.
		Book *newBooks = new Book[newCapacity];
		// Copy the books over to the new array.
		for (int i = 0; i < theSize; ++i)
		{
			newBooks[i] = books[i];
		}
		// Update member variables
		theCapacity = newCapacity;
		delete[] books;
		books = newBooks;
	}
}

/**
 * Add a book to the reading list. If a book with the same ID exists, it is
 * replaced by b.
 *
 * If there is no room to add another book, calls reserve(size()+1)
 * before adding.
 *
@pre size() < capacity() || contains(b.getID())
 * @post contains(b.getID()) && b == get(b.getID())
 */
void ReadingList::add(Book b)
{
	// Look for a matching book
	int insertHere = find(b.getID());
	if (insertHere >= 0)
	{
		// Found one - replace it.
		books[insertHere] = b;
	}
	else
	{
		// No match. Insert this new book into the appropriate
		// location.
		insertHere = theSize;
		reserve(theSize+1);
		while (insertHere > 0 && b < books[insertHere - 1])
		{
			books[insertHere] = books[insertHere - 1];
			--insertHere;
		}
		books[insertHere] = b;
		++theSize;
	}
}

/**
	 * Remove a book from the reading list. If a book with the same ID exists, it is
	 * removed from the list. Otherwise the list is left unchanged.
	 *
	 * @param bid ID of a book to remove
	 *
	 * @post !contains(bid) 
	 */
void ReadingList::remove(std::string bid)
{
	int toRemove = find(bid);
	if (toRemove >= 0)
	{
		// Found the book to remove;
		for (int j = toRemove + 1; j < theSize; ++j)
			books[j - 1] = books[j];
		--theSize;
	}
}

/**
 * Check to see if a book is contained in the reading list.
 *
 * @param bookID the ID string of a potential book
 * @returns true iff a book with that ID is in the reading list.
 */
bool ReadingList::contains(std::string bookID) const
{
	for (int i = 0; i < theSize; ++i)
	{
		if (books[i].getID() == bookID)
			return true;
	}
	return false;
}

/**
 * Return the index (position) of the book with a given ID
 * in the reading list.
 *
 * @param bookID the ID string of a potential book
 * @returns The position of a book with that ID in the reading list, or
 *      -1 if the ID is unknown.
 * 
 * @post find(id) < 0 || get(find(id)).getID() == id
 */
int ReadingList::find(std::string bookID) const
{
	for (int i = 0; i < theSize; ++i)
	{
		if (books[i].getID() == bookID)
			return i;
	}
	return -1;
}

/**
 * Return a book based upon ID ordering.
 *
 * @param i index of a desired book.
 * @return the requested book, or Book() if i is illegal.
 *
 * @pre 0 <= i && i < size()
 * @post if i0 <= i1, get(i0).getID() <= get(i1).getID()
 */
Book ReadingList::get(int i) const
{
	if (i >= 0 && i < theSize)
		return books[i];
	else
		return Book();
}

/**
 * Read a reading list from the input stream, terminating at
 * end of stream or at a book with ID "**END**".
 *
 * @param in the stream to read from.
 * @param readingList the reading list variable to hold the input.
 * @return the stream from which the data was read.
 */
std::istream &operator>>(std::istream &in, ReadingList &readingList)
{
	readingList = ReadingList();
	Book b;
	while (in >> b)
	{
		if (b.getID() != "**END**")
		{
			if (readingList.capacity() <= readingList.size())
			{
				readingList.reserve(2 * readingList.size() + 1);
			}
			readingList.add(b);
		}
		else
		{
			break;
		}
	}
	return in;
}

/**
 * Write a reading list to an output stream.
 *
 * @param out the stream to print to.
 * @param readingList the reading list to be printed.
 * @return the stream to which the data was written.
 *
 */
std::ostream &operator<<(std::ostream &out, const ReadingList &readingList)
{
	for (int i = 0; i < readingList.size(); ++i)
	{
		out << readingList.get(i) << endl;
	}
	return out;
}

/**
 * Compare two reading lists.
 * @param left a reading list
 * @param right a reading list
 * @return True iff both reading lists are of the same length and all corresponding
 *    elements are equal.
 */
bool operator==(const ReadingList &left, const ReadingList &right)
{
	if (left.size() != right.size())
		return false;
	else
	{
		for (int i = 0; i < left.size(); ++i)
		{
			if (!(left.get(i) == right.get(i)))
				return false;
		}
		return true;
	}
}

