#include <bits/stdc++.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>

using namespace std;

int main(int argc, char const *argv[]){
	
	int sock = socket(AF_INET, SOCK_STREAM, 0);

	if(sock<0){
		printf("Socket Error\n");
 		return 0;
	}
	
	struct sockaddr_in server_addr;

	bzero((char *) &server_addr, sizeof(server_addr));

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = INADDR_ANY;
	server_addr.sin_port = 0;

	if(bind(sock, (struct sockaddr *) &server_addr, sizeof(server_addr))<0){
		perror("Bind faild...");
		exit(EXIT_FAILURE);
	}

	socklen_t len = sizeof(server_addr);

	if(getsockname(sock, (struct sockaddr *) &server_addr, &len) == -1 ){

	perror("getsockname");
	return 0;
	}


	printf("\n\nWaiting for client connection...\n\n\nPort:		%d\n\n",ntohs(server_addr.sin_port));

	if(listen(sock, 3) <0)
	{
		perror("Listen");
		exit(EXIT_FAILURE);
	}

	int new_socket, val_read;
	int addr_len = sizeof(server_addr);

	///Waiting for connection with client...



	if((new_socket = accept(sock, (struct sockaddr *) &server_addr, (socklen_t*) &addr_len)) <0){
	
		perror("Accepted");
		exit(EXIT_FAILURE);
	}
	
	printf("Connection Established...\n");
	
	char host_name[200];
	gethostname(host_name, 200);

	///After Establishing Connection...


	char buffer[1024] = {0};
	char sender[1024] = "220 OK, from ";
	
	strcat(sender, host_name);
	send(new_socket, sender, strlen(sender), 0);
	
	
	//Reply of hello...
	
	bzero(buffer, 1024);
	val_read = read(new_socket, buffer, 1024);
	
	bzero(sender, 1024);

	char *pch;
	char p[200];
	pch = strtok(buffer, " ");
	strcpy(p, pch);
	pch = strtok(NULL, " ");
	strcpy(buffer, pch);
	if(strcmp(p,"HELO")==0){
		strcat(sender, "250 Hello ");
		strcat(sender, buffer);
		strcat(sender, ", please to meet you.");
		send(new_socket, sender, strlen(sender) , 0);
	}
	else {
		strcat(sender,"500 Syntax error command unrecognized. Required\"HELO\" command.\n");
		send(new_socket, sender, strlen(sender), 0);
		
		printf("Connection Closed.\n\n");
		return main(argc, argv);
	}


	///Reply of Mail From...


	bzero(buffer, 1024);
	val_read = read(new_socket, buffer, 1024);
	
	bzero(sender, 1024);

	char p1[200];
	pch = strtok(buffer, " ");
	strcpy(p, pch);
	pch = strtok(NULL, " ");

	strcpy(p1, pch);
	pch = strtok(NULL, " ");
	pch = strtok(NULL, " ");

	strcpy(buffer, pch);
		
	if(strcmp(p,"MAIL")==0 && strcmp(p1,"FROM")==0){
		printf("TO: %s\n", buffer);
		
		strcat(sender,"250 ");
		strcat(sender, buffer);
		strcat(sender," ... Sender Ok.");

		send(new_socket, sender, strlen(sender), 0);

	}
	else {
		
		strcat(sender, "500 Syntax error command unrecognized. Required \"MAIL FROM\" command.\n");
		send(new_socket, sender, strlen(sender), 0);
		
		printf("Connection Closed.\n\n");
		
		return main(argc, argv);		
	}


	///Reply of RCPT...

	FILE *file;

	bzero(buffer, 1024);
	val_read = read(new_socket, buffer, 1024);
	bzero(sender, 1024);

	pch = strtok(buffer, " ");
	strcpy(p,pch);
	pch = strtok(NULL, " ");
	strcpy(p1, pch);
	pch = strtok(NULL, " ");
	pch = strtok(NULL, " ");

	strcpy(buffer, pch);

	char receiver[200];

	strcpy(receiver, buffer);

	if((strcmp(p,"RCPT")==0) && strcmp(p1,"TO")==0) {

		
		///Creating file...

		pch = strtok(receiver, "@");

		char file_name[200];
		strcpy(file_name,pch);
		strcat(file_name,".txt");

		file = fopen(file_name, "a");

		if(file==NULL) {
			strcat(sender,"404 can't open users mail file.\n");
			send(new_socket, sender, strlen(sender), 0);
			
			printf("Connection Closed.\n\n");
			
			return main(argc, argv);
		}
		else {

			strcat(sender, "250 ");
			strcat(sender, buffer);
			strcat(sender," ... Recipient Ok.");

			send(new_socket, sender, strlen(sender), 0);
		}
	}
	else {

		strcat(sender, "500 Syntax error command unrecognized. Required \"RCPT TO\" command.\n");

		send(new_socket, sender, strlen(sender), 0);
		printf("Connection Closed.\n\n");

		return main(argc, argv);
	}

	
	///Checking data...

	bzero(buffer, 1024);
	val_read = read(new_socket, buffer, 1024);
	bzero(sender, 1024);

	char *pch1;

	pch1 = strtok(buffer, " ");
	strcpy(p, pch1);
	pch1 = strtok(NULL, " ");

	char header[1024];

	if(strcmp(p,"DATA")==0) {

		strcat(sender, "354 Enter mail, End with \".\" on a line by itself.");

		send(new_socket, sender, strlen(sender), 0);	
	}
	else {

		strcat(sender, "500 Syntax error command unrecognized. Required \"DATA\" command.\n");

		send(new_socket, sender, strlen(sender), 0);

		printf("COnnection Closed.\n");

		return main(argc,argv);
	}


	///Writing to the file...

	int cnt = 0;
	
	while(1) {

		bzero(buffer, 1024);
		val_read = read(new_socket, buffer, 1024);

		if(strcmp(buffer, ".")==0){
			bzero(sender, 1024);
			strcat(sender, "250 Message accepted and stored in user's file. Total message line: ");
			
			char line[200];
			sprintf(line,"%d",cnt);

			send(new_socket, sender, strlen(sender), 0);

			bzero(buffer,1024);
			strcat(buffer, "\n\t***************************\n");
			
			fwrite(buffer, sizeof(char), strlen(buffer), file);

			break;
		}
		
		cnt++;
		fwrite(buffer, sizeof(char), strlen(buffer), file);
	}
	
	fclose(file);

	///Quit...

	bzero(buffer, 1024);
	val_read = read(new_socket, buffer, 1024);
	bzero(sender, 1024);


	if(strcmp(buffer, "QUIT")==0) {
 		
		strcat(sender, "221 ");
		strcat(sender, host_name);
		strcat(sender, " Closing Connection.");
		
		send(new_socket, sender, strlen(sender), 0);
	}
	else {
		
		strcat(sender, "500 Syntax error command unrecognized. Required \"QUIT\" command. \n");

		printf("Connection Closed.\n\n");

		return main(argc, argv);
	}


	close(new_socket);
	close(sock);
	printf("Connection Closed.\n\n");

	return main(argc, argv);
}
	
