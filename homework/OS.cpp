#include <iostream>
#include <cstdlib>
#include <ctime>

using namespace std;

int main(){
	int frames[7] = {10,20,30,40,50,60,70};
	srand(time(NULL));
	for(int i = 0;i < 10;i++){
		cout << rand()%350 + 1 << endl;
	}
	cout << "hello world" << endl;
	//350/6 = 58.3 = 58	350/4 = 87.5 88
	for(int i = 0;i < 5;i++){
		cout << rand()%30 + 58 << endl; 
	}

	system("pause");
	return 0;
}
