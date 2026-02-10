/*
 * readingList.h
 *
 *  Created on: May 11, 2021
 *      Author: zeil
 */

#ifndef READINGLIST_H_
#define READINGLIST_H_

#include <string>
#include <iostream>

#include "book.h"

/**
 * A list of books, identified by Gutenberg ID.
 */
class ReadingList {
public:
	/**
	 * Create a new empty reading list, capable of holding up to
	 * maxCapacity books.
	 *
	 * @param maxCapacity maximum number of books this reading list
	 *    can hold.
	 */
	ReadingList (int maxCapacity = 100);

	// The Big 3
	ReadingList (const ReadingList& c);
	ReadingList& operator= (const ReadingList& c);
	~ReadingList();


	/**
	 * How many books currently in this reading list?
	 *
	 * @return number of books in the reading list.
	 */
	int size() const;

	/**
	 * How many books can this reading list hold?
	 *
	 * @return max capacity of this reading list.
	 */
	int capacity() const;

	/**
	 * Attempt to alter the capacity of this reading list so that
	 * it can hold at least newCapcity books.  Existing books
	 * in the reading list are retained.
	 *
	 * @param newCapacity maximum number of books we would like to be able
	 *    to store in this reading list.
	 * @post capacity() >= newCapacity
	 */
	void reserve (int newCapacity);

	/**
	 * Add a book to the reading list. If a book with the same ID exists, it is
	 * replaced by b. Books are kept in order by their IDs.
	 * 
	 * If there is no room to add another book, calls reserve(size()+1)
	 * before adding.
	 *
	 * @param b A book to add
	 *
	 * @pre size() < capacity() || contains(b.getID())
	 * @post contains(b.getID()) && b == get(b.getID())
	 */
	void add (Book b);

	/**
	 * Remove a book from the reading list. If a book with the same ID exists, it is
	 * removed from the list. Otherwise the list is left unchanged.
	 *
	 * @param bid ID of a book to remove
	 *
	 * @post !contains(bid) 
	 */
	void remove (std::string bid);


	/**
	 * Check to see if a book is contained in the reading list.
	 *
	 * @param bookID the ID string of a potential book
	 * @returns true iff a book with that ID is in the reading list.
	 */
	bool contains(std::string bookID) const;

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
	int find(std::string bookID) const;

	/**
	 * Return a book at a position (based upon ID ordering).
	 *
	 * @param i index of a desired book.
	 * @return the requested book, or Book() if i is illegal.
	 *
	 * @pre 0 <= i && i < size()
	 * @post if i0 < i1, get(i0).getID() < get(i1).getID()
	 */
	Book get(int i) const;

private:
	//** Do NOT change the next three lines
	Book* books;
	int theCapacity;
	int theSize;

	// For use by instructor only
public:
	bool sanityCheck();
};


/**
 * Read a reading list from the input stream, terminating at
 * end of stream or at a book with ID "**END**".
 *
 * @param in the stream to read from.
 * @param readingList the reading list variable to hold the input.
 * @return the stream from which the data was read.
 */
std::istream& operator>> (std::istream& in, ReadingList& readingList);

/**
 * Write a reading list to an output stream.
 *
 * @param out the stream to print to.
 * @param readingList the reading list to be printed.
 * @return the stream to which the data was written.
 *
 */
std::ostream& operator<< (std::ostream& out, const ReadingList& readingList);

/**
 * Compare two reading lists.
 * @param left a reading list
 * @param right a reading list
 * @return True iff both reading lists are of the same length and all corresponding
 *    elements are equal.
 */
bool operator== (const ReadingList& left, const ReadingList& right);








#endif
