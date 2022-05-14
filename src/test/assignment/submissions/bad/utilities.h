#ifndef UTILITIES_H
#define UTILITIES_H

/**
 *  Steven Zeil, Old Dominion University
 *   9/28/2012
 **/

#include <string>

/**
 * @brief Convert all lower-case characters in a string to upper-case.
 * 
 * @param s  a string contianing 0 or more letters.
 * @return string  The same string, unchanged, except that any lower-case alphabetic characters
 *                 have been replaced by their upper-case equivalents. 
 */
std::string toUC (std::string s);



/**
 * @brief Find the shortest string that alphabetically follows a given string.
 * 
 * Ties are broken in favor of the alphabetically smallest such string.
 *
 * @param s a string of upper-case alphabetic characters
 * @return string  The shortest string that alphabetically is equal to or follows this one.
 */
std::string follower (std::string s);

/**
 * @brief Find the shortest string z such that name1 <= z && z < name2.
 * 
 * @param name1 a string of upper-case alphabetic characters
 * @param name2 another string of upper-case alphabetic characters, such that name1 < name2
 * @return std::string the shortest string dividing name1 and name2
 */
std::string findSeparator (std::string name1, std::string name2);


#endif
