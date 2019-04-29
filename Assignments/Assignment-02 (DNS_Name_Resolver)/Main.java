import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

    static ByteArrayOutputStream byteArrayOutputStream;
    static DataOutputStream dataOutputStream;
    static ByteArrayInputStream byteArrayInputStream;
    static DataInputStream dataInputStream;

    static String rootServer[];
    //static String topLevelDomain[];
    //static String topLevelDomain[];
    //static String topLevelDomain[];
    public static void main(String[] args) {

        String domainName="www.google.com";
        String rootServer = "198.41.0.4";
        String topLevelDomain = getIP(domainName,rootServer);
        System.out.println("Top Level Domain Server Address: "+topLevelDomain);
        String authoritativeDomain=getIP(domainName,topLevelDomain);
        System.out.println("Authoritative Domain Server Address: "+authoritativeDomain);
        String ipAddress=getIP(domainName,authoritativeDomain);
        System.out.println("found ip address for  "+domainName+" : "+ipAddress);
        //String domainName = args[1];

    }

    static String getIP(String domainName,String rootServer)
    {
        String ipAddress = "Unknown";
        String domainNameParts[]=domainName.split("\\.");
        byteArrayOutputStream=new ByteArrayOutputStream();
        dataOutputStream=new DataOutputStream(byteArrayOutputStream);
        try{
            dataOutputStream.writeShort(0x1234);
            dataOutputStream.writeShort(0x0000);
            dataOutputStream.writeShort(0x0001);
            dataOutputStream.writeShort(0x0000);
            dataOutputStream.writeShort(0x0000);
            dataOutputStream.writeShort(0x0000);


            int len = domainNameParts.length;
            for(int i=0;i<len;i++)
            {
                byte b[]=domainNameParts[i].getBytes();
                dataOutputStream.writeByte(b.length);
                dataOutputStream.write(b);
            }

            dataOutputStream.writeByte(0x00);
            dataOutputStream.writeShort(0x0001);
            dataOutputStream.writeShort(0x0001);

            byte dnsQueryMessage[]=byteArrayOutputStream.toByteArray();
            int msgLen = dnsQueryMessage.length;


            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(dnsQueryMessage,msgLen, InetAddress.getByName(rootServer),53);
            datagramSocket.send(datagramPacket);

            byte answer[]=new byte[1024];
            DatagramPacket receivedPacked=new DatagramPacket(answer,answer.length);
            datagramSocket.receive(receivedPacked);

            /*System.out.println("received packet size : "+receivedPacked.getLength());
            for(int i=0;i<receivedPacked.getLength();i++)
            {
                System.out.print(String.format("%x",answer[i])+" ");
            }*/

            //System.out.println("\n");
            byteArrayInputStream = new ByteArrayInputStream(answer);
            dataInputStream = new DataInputStream(byteArrayInputStream);

            //System.out.println("Transaction ID : 0x"+String.format("%x",dataInputStream.readShort()));
            //System.out.println("Flags : 0x"+String.format("%x",dataInputStream.readShort()));
            //System.out.println("Questions : 0x"+String.format("%x",dataInputStream.readShort()));
            //System.out.println("Answer RRs : 0x"+String.format("%x",dataInputStream.readShort()));
            //System.out.println("Authority RRs : 0x"+String.format("%x",dataInputStream.readShort()));
            //System.out.println("Additional RRs : 0x"+String.format("%x",dataInputStream.readShort()));
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();


            String queryDomainName = getName();
//            System.out.println("Queries : ");
//            System.out.println("    Name : "+ queryDomainName);
//            System.out.println("    Type : "+ getType(dataInputStream.readShort()));
//            System.out.println("    Class : IN (0x"+String.format("%x",dataInputStream.readShort())+")");
            dataInputStream.readShort();
            dataInputStream.readShort();

            while (true)
            {

                String type="";
                String name = getName();
//                System.out.println("Answer : ");
//                System.out.println("    Name : "+ queryDomainName);
                int t = dataInputStream.readShort();
                type=getType(t);
//                System.out.println("    Type : "+ type);
//                System.out.println("    Class : IN (0x"+String.format("%x",dataInputStream.readShort())+")");
//                System.out.println("    Time to live : "+String.format("%d",dataInputStream.readInt()));
//                System.out.println("    Data length : "+String.format("%d",dataInputStream.readShort()));

                dataInputStream.readShort();
                dataInputStream.readInt();
                dataInputStream.readShort();

                if(type.equals("A"))
                {
                    System.out.println("\n");
                    int a=dataInputStream.readByte();
                    a= a & 0x000000ff;
                    int b=dataInputStream.readByte();
                    b= b & 0x000000ff;
                    int c=dataInputStream.readByte();
                    c= c & 0x000000ff;
                    int d=dataInputStream.readByte();
                    d= d & 0x000000ff;

                    ipAddress=String.format("%d.%d.%d.%d",a,b,c,d);

                    return ipAddress;
                }
                else if(type.equals("AAAA"))
                {
                    int a=dataInputStream.readShort();
                    int b=dataInputStream.readShort();
                    int c=dataInputStream.readShort();
                    int d=dataInputStream.readShort();
                    int e=dataInputStream.readShort();
                    int f=dataInputStream.readShort();
                    int g=dataInputStream.readShort();
                    int h=dataInputStream.readShort();
                    String ipv6Address=String.format("%x:%x:%x:%x:%x:%x:%x:%x",a,b,c,d,e,f,g,h);
                    //System.out.println("IPV6 Address : "+ipv6Address);
                }
                else
                {
                    String nameServer = getName();
                    //System.out.println("    Name Server : "+nameServer);
                }

            }
        }catch (Exception e)
        {
            System.out.println("Error ");
            System.out.println(e);
        }
        return ipAddress;
    }
    static String getName()throws Exception
    {
        byte DomainNameByteArray[] = new byte[1024];
        int i=0;
        while (true)
        {
            byte k=dataInputStream.readByte();
            if(k==0)
            {
                break;
            }
            if(String.format("%x",k).equals("c0"))
            {
                dataInputStream.readByte();
                break;
            }
            if(i>0)
            {
                DomainNameByteArray[i]='.';
                i++;
            }

            for(int j=0;j<k;j++)
            {
                byte b=dataInputStream.readByte();
                DomainNameByteArray[i]=b;
                i++;
            }

        }
        String DomainName = new String(DomainNameByteArray,0,i);
        return DomainName;
    }

    static String getType(int a)
    {
        if(a==1)
        {
            return "A";
        }
        if(a==2)
        {
            return "NS";
        }
        if(a==5)
        {
            return "CNAME";
        }
        if(a==28)
        {
            return "AAAA";
        }
        return "Unknown";
    }
}
