#include "book.h"

using namespace std;


/**
 * Create a book.
 *
 * @param id the Gutenberg ID for this book
 * @param authorInfo the author of the book
 * @param title the title of the book
 */
Book::Book (std::string theId, std::string authorInfo, std::string theTitle)
	: title(theTitle), id(theId), authorName(authorInfo)
{
}


bool Book::operator< (const Book& b) const
{
  return id < b.id;
}


bool Book::operator== (const Book& b) const
{
  return (id == b.id);
}

std::ostream& operator<< (std::ostream& out, const Book& b)
{
  out << b.getID() << "\t"
		  << b.getAuthor()  << "\t"
		  << b.getTitle();
  return out;
}


std::istream& operator>> (std::istream& in, Book& b)
{
  string line;
  getline (in, line);
  if (!in.good())
    return in;
  int tab1 = line.find ("\t");
  int tab2 = line.find ("\t", tab1+1);
  string id = line.substr(0, tab1);
  string author = line.substr (tab1+1, tab2-tab1-1);
  string title = line.substr(tab2+1);
  b.setID (id);
  b.setAuthor (author);
  b.setTitle (title);
  return in;
}

