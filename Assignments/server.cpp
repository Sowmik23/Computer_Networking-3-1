#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>

void error(const char *msg)
{
	perror(msg);
	exit(1);
}

int main(int argc,char *argv[])
{
	if(argc<2)
	{
		fprintf(stderr,"port not provided");
		exit(1);
	}
	int sockfd,newsockfd,portno,n;	
	char buffer[255];

	struct sockaddr_in serv_addr,cli_addr;
	socklen_t client;

	sockfd=socket(AF_INET,SOCK_STREAM,0);
	if(sockfd<0)
	{
		error("error opening socket");
	}

	bzero((char *) &serv_addr,sizeof(serv_addr));
	portno=atoi(argv[1]);
	
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr=INADDR_ANY;
	serv_addr.sin_port=htons(portno);
	
	if(bind(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr))<0) error("Binding failed");
		
	
	listen(sockfd,5);
	client=sizeof(cli_addr);
	newsockfd=accept(sockfd,(struct sockaddr *) &cli_addr,&client);

	if(newsockfd<0) error("Error on accept");
	
	strcpy(buffer,"OK 200");
	write(newsockfd,buffer,strlen(buffer));


	bzero(buffer,256);

	read(newsockfd,buffer,255);
	
	char p[20];
	
	
	 char * pch;
	  pch = strtok (buffer," ");
	  while (pch != NULL)
	  {
	    strcpy(p,pch);
	    pch = strtok (NULL, "\n");
	  }

	  bzero(buffer,256);
	  strcpy(buffer,"250 HELLO ");
	  strcat(buffer,p);
	  strcat(buffer,", pleased to meet you");

	write(newsockfd,buffer,strlen(buffer));


	bzero(buffer,256);

	read(newsockfd,buffer,255); //read mail from:

	bzero(buffer,256);
	strcpy(buffer,"250 OK");
	write(newsockfd,buffer,strlen(buffer));
	
	bzero(buffer,256);
	read(newsockfd,buffer,255);
	
	bzero(buffer,256);
	strcpy(buffer,"250 OK");
	write(newsockfd,buffer,strlen(buffer));
	bzero(buffer,256);
	
	bzero(buffer,256);
	read(newsockfd,buffer,255);
	
	bzero(buffer,256);
	strcpy(buffer,"354 End data with .");
	write(newsockfd,buffer,strlen(buffer));
	bzero(buffer,256);
	
	return 0;
	
	while(1)
	{
		bzero(buffer,256);
		n=read(newsockfd,buffer,255);
		if(n<0)
			error("error on reading");
		printf("Client : %s",buffer);
		bzero(buffer,255);
		fgets(buffer,255,stdin);
		n=write(newsockfd,buffer,strlen(buffer));
		if(n<0)
			error("error on writing");
		int i=strncmp("Bye",buffer,3);
		if(i==0)
			break;
	}
	close(newsockfd);
	close(sockfd);
	
	return 0;
}
