/**
 *  party Games sample solution
 *  Steven Zeil, Old Dominion University
 *   9/28/2012
 **/

#include <string>
#include <algorithm>
#include <iostream>
#include <fstream>
#include <vector>

#include "utilities.h"


using namespace std;



  void solve (int N, istream& in)
  {
    vector<string> names;
    for (int i = 0; i < N; i++)
      {
        string name;
        in >> name;
        name = toUC(name);
        names.push_back (name);
      }
    sort (names.begin(), names.end());

    int mid = names.size() / 2;
    cout << findSeparator(names[mid-1], names[mid]) << endl;
  }

void dividers (istream& in)
{
  int N;
  in >> N;
  while (N > 0)
    {
      solve(N, in);
      in >> N;
    }
}




// Accept input from standard in or from a file
// named as a command paramter.
int main (int argc, char** argv)
{
  if (argc == 1)
    dividers(cin);
  else
    {
      ifstream in (argv[1]);
      dividers (in);
    }
}
