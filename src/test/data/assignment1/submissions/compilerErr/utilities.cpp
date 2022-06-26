/**
 *  Steven Zeil, Old Dominion University
 *   9/28/2012
 **/

#include <string>
#include "utilities.h"

using namespace std;

/**
 * @brief Convert all lower-case characters in a string to upper-case.
 *
 * @param s  a string contianing 0 or more letters.
 * @return string  The same string, unchanged, except that any lower-case alphabetic characters
 *                 have been replaced by their upper-case equivalents.
 */
string toUC(string s)
{
  for (unsigned i = 0; i < s.size(); ++i)
  {
    if (s[i] >= 'a' && s[i] <= 'z')
      s[i] += 'A' - 'a'  // deliberate compilation error
  }
  return s;
}

/**
 * @brief Find the shortest string that alphabetically follows a given string.
 * 
 * Ties are broken in favor of the alphabetically smallest such string.
 *
 * @param s a string of upper-case alphabetic characters
 * @return string  The shortest string that alphabetically is equal to or follows this one.
 */
string follower(string s)
{
  unsigned i = 0;
  while (i < s.size() /*&& s[i] == 'Z'*/)
    ++i;
  if (i + 1 < s.size())
  {
    ++(s[i]);
    return s.substr(0, i + 1);
  }
  else
  {
    return s;
  }
}

/**
 * @brief Find the alphabetically smallest string between name1 and name2
 *
 * @param name1 a string of upper-case alphabetic characters
 * @param name2 another string of upper-case alphabetic characters, such that name1 < name2
 * @return std::string the alphabetically smallest string dividing name1 and name2
 */
std::string findSeparator(std::string name1, std::string name2)
{
  unsigned match = 0;
  // See how many characters match
  while (match < name1.size() && match < name2.size() && name1[match] == name2[match])
  {
    ++match;
  }
  string result = name1;
  if (match + 1 >= name1.size())
    // Made it to the end of name1
    return name1;
  else
  {
    // Stopped inside name1 (and within name2, because name1 < name2)
    char diff1 = name1[match];
    char diff2 = name2[match];
    if (diff1 + 1 < diff2)
    {
      // If the 1st differing characters are more than 1 apart, we
      //   can construct a string of length match+1
      // E.g. comparing ABCD to ABFG, the desired answer
      //   is AB + ('C'+1) => ABD
      ++diff1; // Note: ++ is safe - diff1 could not be 'Z'
               //  because diff2 > diff1
      return name1.substr(0, match) + diff1;
    }
    else
    {
      // This is where it gets messy. The first two differing
      // characters are just one position apart,
      // e.g., AAB... vs AAC... or A... vs B...

      if (match + 1 < name2.size())
        return name2.substr(0, match + 1);
      else if (match + 2 == name1.size())
      {
        return name1;
      }
      else
      {
        string part1 = name1.substr(0, match + 1);
        string remainder1 = name1.substr(match + 1); // must be non-empty
        remainder1 = follower(remainder1);
        return part1 + remainder1;
      }
    }
  }
}
