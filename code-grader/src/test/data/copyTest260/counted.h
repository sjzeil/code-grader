/*
 * counted.h
 *
 *  Created on: Apr 10, 2017
 *      Author: zeil
 */

#ifndef COUNTED_H_
#define COUNTED_H_

/**
 * Utility for detecting memory leaks.
 */
class Counted
{
	/**
	 * A count of the number of Counted objects currently in use.
	 */
	static int numObjects; // = 0;

public:
	Counted () { ++numObjects; }
	Counted (const Counted& c) { ++numObjects; }
	~Counted() {--numObjects;}

	static int getCurrentCount() {return numObjects;}
};



#endif /* COUNTED_H_ */
