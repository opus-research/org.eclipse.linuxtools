#include <iostream>
#include <stdlib.h>

#define SIZE 10

using namespace std;

int main()
{
	char *waste = new char;
	free(waste);
	return 0;
} 
