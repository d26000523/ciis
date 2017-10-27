#include <iostream>
#include <cstdlib>
#include <ctime>

using namespace std;

int main(){
	int frameNum[7] = {10,20,30,40,50,60,70};
	srand(time(NULL));
	
	int requestNum = 70000;
	
	int *request = new int[requestNum];
	cout << "random access" << endl;
	//random access
	for(int i = 0;i < requestNum;i++){
		int num = rand()%350 + 1;
//		cout << num << endl;
		request[i] = num;
	}

	
	
	cout << "locality access" << endl;
	//locality access 
	//350/6 = 58.3 = 58	350/4 = 87.5 88
	for(int i = 0;i < requestNum;i++){
		cout << rand()%30 + 58 << endl; 
	}
	
//	cout << "binary access" << endl;
//	
//	int target = rand()%350 + 1;
//	//int target = 350;
//	int init = rand()%350 + 1;
//	cout << "current node:" << init << endl;
//	
//	cout << "target node:" << target << endl;
//	int root = 1,current = init;
//	//current = 37, target = 86
//	int path[20],pathIndex = 0;
//	for(int i=0;i<20;i++){
//		path[i] = 0;
//	}
//	
//	while(current != root){
//		current /=2;
//		path[pathIndex] = current;
//		pathIndex++;
//		cout << current << endl;
//	}
//	current = target;
//	while(current != root){
//		
//		bool isNew = true;
//		for(int i=0;i<20;i++){
//			if(current == path[i]){
//				//path[i] = 0;
//				for(int j=i;j<20;j++){
//					if(path[j] < current)
//						path[j] = 0;
//				}
//				isNew = false;
//				break;
//			}
//		}
//		if(isNew){
//			path[pathIndex] = current;
//			pathIndex ++;			
//		}
//		else
//			break;
//		
//		cout << current << endl;
//		current /=2;
//	}
//	cout << endl;
//	for(int i=0;i<20;i++){
//			if(path[i] != 0){
//				cout << path[i] << endl;
//			}
//			if(path[i] < path[i+1])
//				break;		
//		}
//	for(int j=19;j>0;j--){
//			if(path[j] != 0){
//				cout << path[j] << endl;
//			}
//			if(path[j] > path[j-1])
//				break;		
//	}
	
	//init frame space and set empty
	int *frame = new int[frameNum[0]];
	for(int i=0;i<frameNum[0];i++)
		frame[i] = 0;
	
	//FIFO
	int pagef = 0;
	int FIFOPos = 0;
	for(int i=0;i<requestNum;i++){
		//cout << "request: " << request[i] << endl; 
		bool inframe = false;
		//check page in frame or not
		for(int j=0;j<frameNum[0];j++){
			if(frame[j] == request[i])
				inframe = true;
		}
		//if not, cause page fault and swap page into frame, FIFO position turn to next
		if(!inframe){
			pagef++;
			//cout << "page fault" << endl;
			frame[FIFOPos] = request[i];
			FIFOPos ++;
			if(FIFOPos == frameNum[0])
				FIFOPos = 0;
		}
		//print frame
//		for(int j=0;j<frameNum[0];j++)
//			cout << frame[j] << " ";
//		cout << endl;
	}
	cout << pagef << endl;
	
//	//OPT
//	
//	//init opt counter
//	int *optCount = new int[frameNum[0]];
//	for(int i=0;i<frameNum[0];i++)
//		optCount[i] = 0;
//		
//	for(int i=0;i<requestNum;i++){
//		cout << "request: " << request[i] << endl;
//		bool inframe = false;
//		//check page in frame or not
//		for(int j=0;j<frameNum[0];j++){
//			if(frame[j] == request[i])
//				inframe = true;
//		}
//		if(!inframe){
//			cout << "page fault" << endl;
//			bool fullframe = true;
//			//if cause cold start page fault, directly load in
//			for(int l=0;l<frameNum[0];l++){
//				if(frame[l] == 0){
//					frame[l] = request[i];
//					fullframe = false;
//					break;
//				}
//			}
//			if(fullframe){
//				//if not, count every frame that when will be referenced
//				for(int j=0;j<frameNum[0];j++){
//						for(int k=i+1;k<=requestNum;k++){
//							optCount[j] ++;
//							if(frame[j] == request[k])
//								break;
//						}
//				}
//				
//				int max = 0,optPos = 0;
//				//select the page that will not be used for the longest period
//				for(int j=0;j<frameNum[0];j++){
//					if(optCount[j] > max){
//						max = optCount[j];
//						optPos = j;
//					}
//				}
//				//select to be victim frame
//				frame[optPos] = request[i];
//				//reset opt counter
//				for(int i=0;i<frameNum[0];i++)
//					optCount[i] = 0;
//			}	
//		}
//		//print frame
//		for(int j=0;j<frameNum[0];j++)
//			cout << frame[j] << " ";
//		cout << endl;
//	}
//	
	
	
//	system("pause");
	return 0;
}

