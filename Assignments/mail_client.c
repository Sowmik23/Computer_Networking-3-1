//#include <bits/stdc++.h>
#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

//using namespace std;

int main(int argc, char const *argv[])
{
	int PORT, sock =0, val_read;
	
	if(argc!=4) {
		printf("Invalid number of arguments. Please input correct arguments.");
		
		exit(0);
	}
	
	char * pch;
	char port[50];
	char receiver[200];
	
	//pch = strtok(argv[1], ":");
	
	pch = strtok (argv[1],":");
	strcpy(port,pch);
	
	PORT = atoi(port);
	
	//struct sockaddr_in serv_add;
	
	struct sockaddr_in serv_addr;
	
	
	char buffer[1024] = {0};
	
	if((sock =socket(AF_INET, SOCK_STREAM, 0)) <0){
		
		printf("\n\nSocket Creation Error!!!\n");
		return -1;
	}
	
	memset( &serv_addr, '0', sizeof(serv_addr));
	
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(PORT);
	
	if(inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr) <=0) {
		
		printf("\n\nInvalid address\\ Address not supported \n");
		return -1;
	}
	if(connect(sock, (struct sockaddr *) &serv_addr, sizeof(serv_addr))<0 ) {
		
		printf("\nConnection Failed. \n");
		return -1;
	}
	
	///checking connection...
	
	val_read = read( sock, buffer, 1024);
	
	printf("Server : %s\n",buffer);
	if(buffer[0]!='2') {
		
		close(sock);
		exit(0);
	}
	
	char host_name[200];
	gethostname(host_name);
	
	printf("Client : HELO %s\n",host_name);
	
	
	///Checking HELO message...
	
	char hello[200] = "HELO ";
	
	strcat(hello, host_name);
	send(sock, hello, strlen(hello), 0);
	
	bzero(buffer, 1024);
	
	val_read = read(sock, buffer, 1024);
	
	printf("Server : %s\n",buffer);
	
	if(buffer[0]!='2') {
		close(sock);
		exit(0);
	}
	
	
	///checking MAIL FROM...
	
	
	char user_name[200];
	
	//getlogin (user_name);
	getlogin_r(user_name);
	char mail_from[200] = "MAIL FROM : ";
	
	strcat(mail_from, user_name);
	strcat(mail_from,"@");
	strcat(mail_from, host_name);
	
	printf("Client : %s\n",mail_from);
	
	send(sock, mail_from, strlen(mail_from), 0);
	
	bzero(buffer, 1024);
	
	val_read = read(sock, buffer, 1024);
	
	printf("Server : %s\n", buffer);
	
	if(buffer[0]!='2') {
		close(sock);
		exit(0);
	}
	
	
	///checking RCPT TO ...
	
	char rcpt_to[200] = "RCPT TO : ";
	strcat(rcpt_to, receiver);
	
	printf("Client : %s\n",rcpt_to);
	
	send(sock, rcpt_to, strlen(rcpt_to), 0);
	
	bzero(buffer, 1024);
	
	val_read = read(sock, buffer, 1024) ;
	
	printf("Server : %s\n",buffer);
	if(buffer[0]!='2') {
		close(sock);
		exit(0);
	}
	
	///checking data...
	
	
	char data[200] = "DATA";
	printf("Client : %s\n",data);
	
	send(sock, data, strlen(data), 0);
	
	bzero(buffer, 1024);
	
	val_read = read(sock, buffer, 1024);
	printf("Server : %s\n",buffer);
	if(buffer[0]!='2' && buffer[0]!='3') {
		close(sock);
		exit(0);
	}
	
	
	///file header...
	
	char header[1024];
	bzero(header, 1024);
	
	///Set time...
	
	char date[200];
	time_t t = time(0);
	struct tm = tstruct;
	tstruct = *localtime( &t);
	strftime(date, sizeof(date), "%Y-%m-%d; Time: %X", &tstruct);
	
	strcat(header, "TO : ");
	strcat(header, receiver);
	strcat(header, "\n");
	strcat(header, "FROM : ");
	strcat(header, user_name);
	strcat(header, "@");
	strcat(header, host_name);
	strcat(header, "\n");
	strcat(header, "Subject : ");
	strcat(header, argv[2]);
	strcat(header, "\n");
	strcat(header, "Date : ");
	strcat(header, date);
	strcat(header, "\n\n\n");
	
	send(sock, header, sizeof(header),0);

	
	///Reading data from file...
	
	
	FILE *f = fopen(argv[3], "r");
	char temp[1024];
	bzero(temp,1024);
	
	while(f!=NULL && fgets(temp, sizeof(temp), f) != NULL) {
		
		printf("Client : %s\n", temp);
		send(sock, temp, sizeof(temp), 0);
	}
	
	bzero(temp, 1024);
	strcat(temp, ".");
	
	send(sock, temp, sizeof(temp), 0);
	
	bzero(buffer, 1024);
	val_read =  read(sock, buffer, 1024);
	printf("Server : %s\n",buffer);
	
	if(f!=NULL) {
		
		fclose(f);
	}
	
	
	///QUIT...
	
	char quit[200] = "QUIT";
	
	printf("Client : %s\n",quit);
	
	send(sock, quit, strlen(quit), 0);
	bzero(buffer, 1024);
	
	val_read = read(sock, buffer, 1024);
	
	bzero(buffer, 1024);
	
	val_read = read(sock, buffer, 1024);
	
	printf("Server : %s\n",buffer);
	if(buffer[0]!='2'){
		close(sock);
		exit(0);
	}
	
	return 0;
}

