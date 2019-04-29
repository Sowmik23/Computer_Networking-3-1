#include <bits/stdc++.h>

#include <unistd.h>
#include <limits.h>

#include <Date_Time.h>
#include <Config.h>

#inclue <header.h>
#inclue <ipAddress.h>
#inclue <DataTransfer.h>


using namespace std;

void quit(int arg) {
	
	printf("S: Caught singnal (%d). Mail Server is shutting down...\n\n\n",arg);
}

int main()
{
	//server and client socket varaibles;
	
	int server_sockfd, client_sockfd;
	socklen_t sin_size;
	
	struct sockaddr_in server_addr , client_addr;
	
	memset(&server_addr, 0, sizeof(server_addr));
	
	
	//creating socket;
	
	if((server_sockfd = socket(AF_INET, SOCK_STREAM, 0)) ==-1) {
		perror("Failed to create socket.\n");
		exit(1);
	}
	
	//set socket's attributes;
	
	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(PORT);
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	bzero(&(server_addr.sin_zero), 8);
	
	//create link;
	
	if(bind(server_sockfd, (struct sockaddr *) &server_addr, sizeof(struct sockaddr)) ==-1) {
		perror("Socket Bind Error.\n");
		exit(1);
	}
	
	//set to non-blocking to avoid lockout issue;
	
	fcntl(server_sockfd, F_SETFL, fcntl(server_sockfd, F_GETFL, 0)| O_NONBLOCK);
	
	//listening requests from clients;
	
	if(listen(server_sockfd, MAX_CLIENTS -1) ==-1) {
		printf("Socket Listen Error.\n");
		exit(1);
	}
	
	//accept requests from the clients and the whole process;
	
	sin_size = sizeof(client_addr);
	gethostname(hostname, HOST_NAME_MAX);
	
	
	printf("Host Name: %s 	IP Address: %s	 Port:  \n\n",hostname,ipAddress(),	PORT);
	
	
	while(1){
		
		if((client_sockfd = accept(server_sockfd, (struct sockaddr *) &client_addr, &sin_size)) ==-1){
			sleep(1);
			continue;
		}
		cout<<"S: Received a connection from "<<inet_ntoa(client_addr.sin_addr)<<" at "<<find_Time()<<endl;
		
		
		mail_proc(client_sockfd);
		
	}
	
	close(client_sockfd);
	
	return 0;
}
