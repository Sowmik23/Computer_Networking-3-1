#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<string.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<netdb.h>
using namespace std;

void error(const char *msg)
{
	perror(msg);
	exit(1);
}

int main(int argc,char *argv[])
{
	int sockfd,portno,n;
	struct sockaddr_in serv_addr;
	struct hostent *server;
	char  buffer[255];

	if(argc<4)
	{
		fprintf(stderr,"usage %s hostname port \n", argv[1]);
		exit(0);
	}

	///portno=atoi(argv[2]);
	char p[10];


	 char * pch;
	  pch = strtok (argv[1],":");
	  while (pch != NULL)
	  {
	    strcpy(p,pch);
	    pch = strtok (NULL, ":");
	  }

	printf("%s\n",p);

	portno=atoi(p);

	sockfd=socket(AF_INET,SOCK_STREAM,0);

	if(sockfd<0) error("error opening socket");

	server=gethostbyname("127.0.0.1");

	if(server==NULL) fprintf(stderr,"Error,no such host");

	bzero((char *) &serv_addr,sizeof(serv_addr));
	serv_addr.sin_family=AF_INET;
	bcopy((char *) server->h_addr,(char *)&serv_addr.sin_addr,server->h_length);
	serv_addr.sin_port=htons(portno);

	if(connect(sockfd,(struct sockaddr *)&serv_addr,sizeof(serv_addr))<0) error("connection failed");
	
	
		bzero(buffer,255);
		read(sockfd,buffer,255);
		printf("Server : %s",buffer);//new line tule diche

		bzero(buffer,255);

		//fgets(buffer,255,stdin);
		write(sockfd,buffer,strlen(buffer));

		bzero(buffer,255);
	
		read(sockfd,buffer,255);
		printf("Server : %s",buffer);//new line tule diche

		bzero(buffer,255);

		strcpy(buffer,"MAIL FROM: ");
	
		write(sockfd,buffer,strlen(buffer));

		bzero(buffer,255);
	
	
		read(sockfd,buffer,255);
		printf("Server : %s",buffer); //new line tule diche

		bzero(buffer,255);
	
		strcpy(buffer,"RCPT to");
		write(sockfd,buffer,strlen(buffer));
		bzero(buffer,255);
	
		read(sockfd,buffer,255);
		printf("Server : %s",buffer);  //new line tule dicheu
		bzero(buffer,255);
	
		strcpy(buffer,"DATA");
		write(sockfd,buffer,strlen(buffer));
		bzero(buffer,255);
	
		read(sockfd,buffer,255);
		printf("Server : %s",buffer);//new line tule diche
		bzero(buffer,255);
	
		 char cwd[256];



	getcwd(cwd, sizeof(cwd));


	
	strcat(cwd,"/");
	strcat(cwd,argv[3]);
	printf("%s\n",cwd);

	FILE * pFile;
	char ab[111];
	pFile = fopen (cwd,"r");

	if (pFile!=NULL)
	{
		fscanf(pFile,"%s\n",&ab);
		printf("%s\n",ab);

	}
	fclose (pFile);
	close(sockfd);

	return 0;
}
