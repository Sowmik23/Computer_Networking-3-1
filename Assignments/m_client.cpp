#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <string.h>
#include <time.h>



int main(int argc, char const *argv[])
{
    int PORT;

    if(argc!=4){
        printf("Invalid number of arguments.Please input correct arguments.");
        exit(0);
    }

    char * pch;

	char port[200];
	char receiver[250];

	pch = strtok (argv[1],":");
	strcpy(receiver,pch);
	pch = strtok (NULL, ":");
	strcpy(port,pch);

	PORT= atoi(port);

	struct sockaddr_in serv_add;
	int sock = 0, valread;
	struct sockaddr_in serv_addr;
	
	char buffer[1024] = {0};
	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0)
	{
		printf("\n Socket creation error \n");
		return -1;
	}

	memset(&serv_addr, '0', sizeof(serv_addr));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(PORT);

	if(inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr)<=0)
	{
		printf("\nInvalid address/ Address not supported \n");
		return -1;
	}

	if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
	{
		printf("\nConnection Failed \n");
		return -1;
	}

	/*****Connection Check msg******/

	valread = read( sock , buffer, 1024);

	printf("Server : %s\n",buffer );
	if(buffer[0]!='2')
	{
		close(sock);
		exit(0);
	}
	char hostname[250];
	gethostname(hostname);
	printf("Client : HELO %s\n",hostname);

		/*****HELO message check******/

	char hello[250]="HELO ";
	strcat(hello,hostname);
	send(sock ,hello, strlen(hello) , 0 ); 

	bzero(buffer,1024);

	valread = read( sock , buffer, 1024);
	printf("Server : %s\n",buffer );
	if(buffer[0]!='2')
	{
		close(sock);
		exit(0);
	}


		/*****MAIL FROM check******/

	char username[250];
	getlogin_r(username);
	char mailfrom[250]="MAIL FROM : ";
	strcat(mailfrom,username);
	strcat(mailfrom,"@");
	strcat(mailfrom,hostname);

	printf("Client : %s\n",mailfrom);
	
	send(sock ,mailfrom, strlen(mailfrom) , 0 ); 

	bzero(buffer,1024);

	valread = read( sock , buffer, 1024);
	printf("Server : %s\n",buffer );
	if(buffer[0]!='2')
	{
		close(sock);
		exit(0);
	}

		/*****RCPT TO check******/

	char rcptto[250]="RCPT TO : ";
	strcat(rcptto,receiver);
	printf("Client : %s\n",rcptto);
	
	send(sock ,rcptto, strlen(rcptto) , 0 ); 

	bzero(buffer,1024);

	valread = read( sock , buffer, 1024);
	printf("Server : %s\n",buffer );
	if(buffer[0]!='2')
	{
		close(sock);
		exit(0);
	}
	
		/*****DATA check******/

	char data[250]="DATA";
	printf("Client : %s\n",data);

	
	send(sock ,data, strlen(data) , 0 ); 

	bzero(buffer,1024);

	valread = read( sock , buffer, 1024);
	printf("Server : %s\n",buffer );
	if(buffer[0]!='2' && buffer[0]!='3')
	{
		close(sock);
		exit(0);
	}

	/*************** File Header *****************/

	char header[1024];
	bzero(header,1024);

          /**********Time Setting *********/

	char date[250];

	time_t     now = time(0);
    struct tm  tstruct;
    tstruct = *localtime(&now);
    strftime(date, sizeof(date), "%Y-%m-%d ;   Time: %X", &tstruct);


	strcat(header,"To : ");
	strcat(header,receiver);
	strcat(header,"\n");
	strcat(header,"From : ");
	strcat(header,username);
	strcat(header,"@");
	strcat(header,hostname);
	strcat(header,"\n");
	strcat(header,"Subject : ");
	strcat(header,argv[2]);
	strcat(header,"\n");
	strcat(header,"Date : ");
	strcat(header,date);
	strcat(header,"\n\n");

     send(sock,header,sizeof(header),0);



	/*************** data read from file *****************/

		FILE *filename=fopen(argv[3],"r");
       char temp[1024];
       bzero(temp,1024);

    while (filename!=NULL && fgets(temp, sizeof(temp),filename) != NULL)
    {

      printf("Client : %s\n",temp);
      send(sock,temp,sizeof(temp),0);
    }

    bzero(temp,1024);
    strcat(temp,".");

    send(sock,temp,sizeof(temp),0);


    bzero(buffer,1024);
	valread = read( sock , buffer, 1024);
	printf("Server : %s\n",buffer );

    if (filename!=NULL) 
  	fclose(filename);

	
	/*****QUIT check******/

	char quit[250]="QUIT";
	printf("Client : %s\n",quit);
	
	send(sock ,quit, strlen(quit) , 0 ); 

	bzero(buffer,1024);

	valread = read( sock , buffer, 1024);
	printf("Server : %s\n",buffer );
	if(buffer[0]!='2')
	{
		close(sock);
		exit(0);
	}

	return 0;
}
