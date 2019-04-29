import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;


public class DNSresolver
{
    static boolean flag = false,CnameFlag=false,SoaFlag=false;
    private static int PortAddressOfDnsServer = 53;
    //Here,
    //flag ensures that type A record found
    //Again, CnameFlag ensures that canonical name found for a domain name
    //SoaFlag ensures SOA type record responded by the server

    private static String RootAddressOfDNS = "198.97.190.53";

    private static String domain = "";

    static String ans = "";
    //ans contains canonical name of a domain name
    static ArrayList<String> Atype = new ArrayList<>();
    //Atype list contains all the A type record values
    static ArrayList<String> temp;
    public static void main(String[] args) throws IOException
    {
        domain = args[0];
        DatagramSocket socket = new DatagramSocket();
        sendRequest(socket, RootAddressOfDNS, domain);
        DataInputStream dis = getResponse(socket);
        int[] results = DnsResHeader(dis);
        int authoritative = results[1], answers = results[0], additional = results[2];
        for (int i=0; i<answers; i++)
        {
            printDNSAnswer(dis);
        }
        for (int i=0; i<authoritative; i++)
        {
            printDNSAnswer(dis);
        }
        for (int i=0; i<additional; i++)
        {
            printDNSAnswer(dis);
        }

        //Above part will finds TLD servers

        if(SoaFlag)
        {
            System.out.println("Domain Not Exists.");
            return;
        }
        else if(flag)
        {
            flag = false;
            temp = OneListToAnother(Atype);
            Atype.clear();
            for(int j=0; j<temp.size(); j++)
            {
                sendRequest(socket, temp.get(j), domain);//find authoritative server from TLD servers
                dis = getResponse(socket);
                results = DnsResHeader(dis);
                answers = results[0];
                authoritative = results[1];
                additional = results[2];
                for (int i = 0; i < answers; i++)
                {
                    printDNSAnswer(dis);
                }
                for (int i = 0; i < authoritative; i++)
                {
                    printDNSAnswer(dis);
                }
                for (int i = 0; i < additional; i++)
                {
                    printDNSAnswer(dis);
                }
                if(flag)
                {
                    break;
                }
            }
        }
        else {
            if(CnameFlag)
            {
                while(true) //finds Type A record following the canonical name chain
                {
                    if(CnameFlag)
                    {
                        CnameFlag=false;
                        sendRequest(socket, RootAddressOfDNS, ans);
                        dis = getResponse(socket);
                        results = DnsResHeader(dis);
                        answers = results[0];
                        authoritative = results[1];
                        additional = results[2];
                        for (int i = 0; i < answers; i++)
                        {
                            printDNSAnswer(dis);
                        }
                        for (int i = 0; i < authoritative; i++)
                        {
                            printDNSAnswer(dis);
                        }
                        for (int i = 0; i < additional; i++)
                        {
                            printDNSAnswer(dis);
                        }
                    }
                    else if(flag)
                    {
                        if (Atype.size() != 0)
                            System.out.println( domain + " = " + Atype.get(0));
                        else
                            System.out.println("DNS Error Occurred");
                        return ;
                    }
                    else
                        break;

                }
            }
            else
                System.out.println("DNS Error Occurred");
            return ;
        }

        //Above portion finds Authoritative servers

        if(SoaFlag)
        {
            System.out.println("Domain Not Exists.");
            return;
        }
        if(flag)
        {
            flag = false;
            temp.clear();
            temp = OneListToAnother(Atype);
            Atype.clear();
            for(int j=0; j<temp.size(); j++)
            {
                sendRequest(socket, temp.get(j), domain);
                dis = getResponse(socket);
                results = DnsResHeader(dis);
                answers = results[0];
                authoritative = results[1];
                additional = results[2];
                for (int i = 0; i < answers; i++) printDNSAnswer(dis);
                if(SoaFlag)
                {
                    System.out.println("Domain Not Exists.");
                    return;
                }
                for (int i = 0; i < authoritative; i++) printDNSAnswer(dis);
                if(SoaFlag)
                {
                    System.out.println("Domain Not Exists.");
                    return;
                }
                for (int i = 0; i < additional; i++) printDNSAnswer(dis);
                if(SoaFlag)
                {
                    System.out.println("Domain Not Exists.");
                    return;
                }
                if(flag) break;
            }
        }
        else{
            if(CnameFlag)
            {
                while(true)
                {
                    if(CnameFlag)
                    {
                        CnameFlag=false;
                        sendRequest(socket, RootAddressOfDNS, ans);
                        dis = getResponse(socket);
                        results = DnsResHeader(dis);
                        answers = results[0];
                        authoritative = results[1];
                        additional = results[2];
                        for (int i = 0; i < answers; i++) printDNSAnswer(dis);
                        if(SoaFlag)
                        {
                            System.out.println("Here Domain Not Exists.");
                            return;
                        }
                        for (int i = 0; i < authoritative; i++) printDNSAnswer(dis);
                        if(SoaFlag)
                        {
                            System.out.println("Here Domain Not Exists.");
                            return;
                        }
                        for (int i = 0; i < additional; i++) printDNSAnswer(dis);
                        if(SoaFlag)
                        {
                            System.out.println("Here Domain Not Exists.");
                            return;
                        }
                    }
                    else if(flag)
                    {
                        if (Atype.size() != 0)
                            System.out.println( domain + " = " + Atype.get(0));
                        else
                            System.out.println("Here DNS Error Occurred");
                        return ;
                    }
                    else
                        break;

                }
            }
            else
                System.out.println("Here DNS Error Occurred");
            return ;
        }
        if(Atype.size()!=0)
            System.out.println(domain+" = "+Atype.get(0));
        else
            System.out.println("Here DNS Error Occurred");

        //Above portion resolves ip address for domain
    }

    //This function sends request to given DNSAddress
    private static DatagramSocket sendRequest(DatagramSocket socket, String DNSAddress, String domain) throws IOException
    {
        InetAddress ipAddress = InetAddress.getByName(DNSAddress);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        /* *** Build a DNS Request Frame **** */
        dos.writeShort(0x1234);			// Identifier:
        dos.writeShort(0x0000);			// Write Query Flags
        dos.writeShort(0x0001);			// Question Count
        dos.writeShort(0x0000);			// Answer Record Count
        dos.writeShort(0x0000);			// Authority Record Count
        dos.writeShort(0x0000);			// Additional Record Count

        String[] domainParts = domain.split("\\.");
        for (int i = 0; i < domainParts.length; i++)
        {
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }

        dos.writeByte(0x00);			// Zero byte to end the header

        dos.writeShort(0x0001);			// Write Type 0x01 = A
        dos.writeShort(0x0001);			// Write Class 0x01 = IN

        byte[] dnsFrame = baos.toByteArray();

        /* *** Send DNS Request Frame *** */
        DatagramPacket Dns_Req_Packet = new DatagramPacket(dnsFrame, dnsFrame.length,
        ipAddress, PortAddressOfDnsServer);
        socket.send(Dns_Req_Packet);

        return socket;
    }

    // Await response from DNS server
    private static DataInputStream getResponse(DatagramSocket socket) throws IOException
    {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        DataInputStream dis = new DataInputStream(bais);

        return dis;
    }

    //This function unfolds the response header
    private static int[] DnsResHeader(DataInputStream dis) throws IOException
    {
        dis.readShort();
        dis.readShort();
        dis.readShort();
        short answers = dis.readShort();
        short authoritative = dis.readShort();
        short additional = dis.readShort();

        int domainPartsLength;
        StringBuilder domainSB = new StringBuilder();
        while ( (domainPartsLength = dis.readByte()) > 0 )
        {
            byte[] domainBytes = new byte[domainPartsLength];
            for (int i=0; i<domainPartsLength; i++) domainBytes[i] = dis.readByte();
            String domain = new String(domainBytes, "UTF-8");
            if (domainSB.length() > 0) domainSB.append(".");
            domainSB.append(domain);
        }
        String responseDomain = domainSB.toString();
        dis.readInt();

        int[] results = new int[3];
        results[0] = answers;
        results[1] = authoritative;
        results[2] = additional;
        return results;
    }


    //This function unfolds Answers,Authoritative and Additional Information section
    private static String printDNSAnswer(DataInputStream dis) throws IOException
    {
        try {
            dis.readShort();
            short type = dis.readShort();
            dis.readShort();
            dis.readInt();

            try {
                if (type == 0x0001 || type == 0x0028)
                {
                    short addrLen = dis.readShort();
                    byte[] address = new byte[addrLen];
                    dis.read(address, 0, addrLen);
                    if (!flag)
                    {
                        Atype.add(Array_to_Ip(address, addrLen));
                        flag = true;
                        return Atype.get(Atype.size() - 1);
                    }
                    else
                    {
                        Atype.add(Array_to_Ip(address, addrLen));
                        return Atype.get(Atype.size() - 1);
                    }
                }
                else if (type == 0x0005)
                {
                    if (!CnameFlag)
                    {
                        ans = getDomain(dis);
                        CnameFlag = true;
                        return ans;
                    }
                    else
                        return getDomain(dis);
                }
                else if (type == 0x0006)
                {
                    short addrLen = dis.readShort();
                    byte[] address = new byte[addrLen];
                    dis.read(address, 0, addrLen);
                    SoaFlag = true;
                    return null;
                }
                else {
                    short addrLen = dis.readShort();
                    byte[] address = new byte[addrLen];
                    dis.read(address, 0, addrLen);
                    return null;
                }
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return null;
    }


    //This function converts input ByteArray To Ip
    private static String Array_to_Ip(byte[] address, short addrLen)
    {
        StringBuilder string_Builder = new StringBuilder();
        for (int i = 0; i < addrLen; i++ )
        {
            if (i != 0) string_Builder.append(".");
            string_Builder.append( String.format("%d", (address[i] & 0xFF)) );
        }
        String output = string_Builder.toString();
        return output;
    }

    //This function swaps list items from one list to another
    private static ArrayList<String> OneListToAnother (ArrayList<String> conatiner)
    {
        ArrayList<String> tmp_Contain = new ArrayList<>();
        for(int i=0; i<conatiner.size(); i++)
        {
            tmp_Contain.add(conatiner.get(i));
        }
        return tmp_Contain;
    }

    public static String getDomain(DataInputStream dis) throws IOException
    {
        int d_len;
        StringBuilder domain_SB = new StringBuilder();
        d_len = dis.readShort();
        byte[] dom_Byte = new byte[d_len];
        for (int i=0; i<d_len; i++)
        {
            dom_Byte[i] = dis.readByte();
        }
        String domain = new String(dom_Byte, "UTF-8");
        if (domain_SB.length() > 0)
        {
            domain_SB.append(".");
        }
        domain_SB.append(domain);
        // dis.readInt();

        return domain_SB.toString();
    }
}
