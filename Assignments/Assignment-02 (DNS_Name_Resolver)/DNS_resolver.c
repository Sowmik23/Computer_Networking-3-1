/*DNS Query on Linux */

/*Author: Sowmik Sarker
 * Email: sowmiksarker.2355.csedu@gmail.com
*/ 

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <unistd.h>



#define type_A 1
#define typeNS 2
#define typeCNAME 3
#define typeSOA 4

char dns_servers[10][100];
int dns_server_cnt = 0;

void Get_DNS_Servers();
void Get_Host_By_Name(unsigned char* , int);
void ChangeToDnsNameFormat(unsigned char *dns, unsigned char *host);
unsigned char* ReadName(unsigned char* reader, unsigned char* buffer, int* cnt);

/* DNS Header Structure */
struct DNS_HEADER
{
	unsigned short id; //identification number
	
	unsigned char rd :1; //recursion desired
	unsigned char tc :1; //truncated message
	unsigned char aa :1; //authoritive answer 
	unsigned char opcode :4; //purpose of message
	unsigned char qr :1; //query/response flag
	
	unsigned char rcode :4; //response code
	unsigned char cd :1; //checking disabled 
	unsigned char ad :1; //authenticated data
	unsigned char z :1; //its z! reserved
	unsigned char ra :1; //recursion available
	
	unsigned short q_cnt; //number of question entries
	unsigned short ans_cnt; //number of answer entries
	unsigned short auth_cnt; //number of authority entries
	unsigned short add_cnt; //number of resource entries
};

struct QUESTION
{
	unsigned short qtype;
	unsigned short qclass;
};


struct R_DATA
{
	unsigned short type;
	unsigned short _class;
	unsigned int ttl;
	unsigned short data_len;
};

struct RES_RECORD 
{
	unsigned char *name;
	struct R_DATA *resource;
	unsigned char *rdata;
};

typedef struct
{
	unsigned char *name;
	struct 	QUESTION *ques;
} QUERY;


int main(int argc, char *argv[]) 
{	
	unsigned char host_name[100];
	
	/*Get the DNS servers from the resolv.conf file on linux */
	Get_DNS_Servers();
	
	printf("Enter Host Name to DNS Lookup : ");
	
	/*Get the input host_name through terminal*/
	scanf("%s",host_name);
	
	/*Get the IP of the input host_name (A type record) */
	
	Get_Host_By_Name(host_name, type_A);
	
	return 0;
}


void Get_DNS_Servers()
{
	FILE *file ;
	char line[300], *ptr;
	
	if((file = fopen("/etc/resolv.conf", "r"))==NULL)
	{
		printf("Failed to opening /etc/resolv.conf file\n");
	}
	
	while(fgets(line, 300, file))
	{
		if(line[0] == '#')
		{
			continue;
		}
		if(strncmp(line, "nameserver", 10)==0)
		{
			ptr = strtok(line, " ");
			ptr = strtok(NULL, " ");
			
			/*Now ptr is the DNS IP (127.0.1.1 in my pc)*/
		}
	}
	
	/*OpenDNS addresses, 208.67.222.222 and 208.67.220.220,
	 *in the Preferred DNS server and Alternate DNS server fields.*/
	 
	strcpy(dns_servers[0], "208.67.222.222");
	strcpy(dns_servers[1], "208.67.220.220");
}
	

void Get_Host_By_Name(unsigned char *host_name, int typeA)	
{
	unsigned char buffer[65536] , *reader, *qname;
	
	struct sockaddr_in a;
	struct RES_RECORD answers[20];
	struct sockaddr_in destination;
	struct DNS_HEADER *dns = NULL;
	
	printf("Resolving %s",host_name);
	
	int sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	
	destination.sin_family = AF_INET;
	destination.sin_port = htons(53);
	destination.sin_addr.s_addr = inet_addr(dns_servers[0]); /*DNS servers*/
	
	qname = (unsigned char*)&buffer[sizeof(struct DNS_HEADER)];
	
	ChangeToDnsNameFormat(qname, host_name);
	
	printf("\nSending Packet...");
	if(sendto(sock,(char*)buffer, sizeof(struct DNS_HEADER) + (strlen((const char*) qname) + 1) + sizeof(struct QUESTION), 0, (struct sockaddr*)&destination, sizeof(destination))<0)
	{
		perror("sendto failed");
	}
	printf("Done");
	
	
	// Receiving the answer //
	
	int ans = sizeof destination;
	
	printf("\nReceiving answer...");
	if(recvfrom(sock, (char*) buffer, 65536, 0, (struct sockaddr*) &destination, (socklen_t*)&ans ) <0)
	{
		perror("recvfrom failed");
	}
	printf("Done");
	
	dns = (struct DNS_HEADER*) buffer;
	
	int flag = 0;
	
	for(int i=0;i<ntohs(dns->ans_cnt);i++)
	{
		answers[i].name = ReadName(reader, buffer, &flag);
		reader = reader + flag;
		
		answers[i].resource = (struct R_DATA*) (reader);
		reader = reader + sizeof(struct R_DATA);
		
		if(ntohs(answers[i].resource->type) == 1) //if it is type A
		{
			answers[i].rdata = (unsigned char*) malloc(ntohs(answers[i].resource->data_len));
			
			for(int j=0;j<ntohs(answers[i].resource->data_len);j++)
			{
				answers[i].rdata[j] = reader[j];
			}
			
			answers[i].rdata[ntohs(answers[i].resource->data_len)] = '\0';
			reader = reader + ntohs(answers[i].resource->data_len);
		}
		else 
		{
			answers[i].rdata = ReadName(reader, buffer , &flag);
			reader = reader + flag;
		}
	}
	
	/*print answers*/
	
	printf("\nAnswer Records : %d \n", ntohs(dns->ans_cnt));
	for(int i=0;i<ntohs(dns->ans_cnt); i++)
	{
		printf("Name : %s ", answers[i].name);
		
		if(ntohs(answers[i].resource->type) == type_A) //if IPv4 address
		{
			long *p;
			p = (long*) answers[i].rdata;
			a.sin_addr.s_addr = (*p); //working without ntohl
			printf("has IPv4 address : %s", inet_ntoa(a.sin_addr));
		}
		if(ntohs(answers[i].resource->type) == 3) //if canonical name
		{
			printf("has alias name : %s",answers[i].rdata);
		}
		printf("\n");
	}
	
	return ;
}
			
unsigned char* ReadName(unsigned char* reader, unsigned char* buffer, int* cnt)
{
	unsigned char *name;
	unsigned int p, jumped, offset;
	p = 0;
	jumped = 0;
	
	*cnt = 1;
	name = (unsigned char*) malloc(256);
	
	name[0] = '\0';
	
	/*read the names in 3www6google3com format*/
	
	while(*reader!=0)
	{
		if(*reader>= 192)
		{
			offset = (*reader) * 256 + *(reader + 1) - 49152;  //49152 = 11000000 00000000
			reader = buffer + offset - 1;
			jumped = 1;  //we have jumped to another location so counting won't go up
		}
		else 
		{
			name[p++] = *reader;
		}
		
		reader++;
		
		if(jumped ==0)
		{
			*cnt = *cnt + 1; //cnt total jumping location
		}
	}
	
	name[p] = '\0'; //end of the string
	if(jumped ==1)
	{
		*cnt = *cnt + 1; //number of steps we actually moved forward in the packet
	}
	
	/* now convert 3www6google3com into www.google.com */
	
	
	int len = strlen((const char*) name);
	
	int i;
	
	for(i=0;i<len;i++)
	{
		p = name[i];
		for(int j=0; j<(int)p ;j++)
		{
			name[i] = name[i+1];
			i++;
		}
		name[i] = '.';
	}
	name[i-1] = '\0'; //for removing the last dot
	
	return name;
}
	
/* This willl convert www.google.com to 3www6google3com */	
		
void ChangeToDnsNameFormat(unsigned char *dns, unsigned char *host)
{
	int k = 0;
	strcat((char*) host,".");
	
	for(int i=0;i<strlen((char*)host);i++)
	{
		if(host[i]=='.')
		{
			*dns++ = i-k;
			for(;k<i;k++)
			{
				*dns++=host[k];
			}
			k++;
		}
	}
	*dns++ = '\0';
}
		
		

