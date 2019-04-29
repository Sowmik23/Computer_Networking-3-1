/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    Socket socket;
    ServerSocket serverSocket;

    File htmlFile;
    File file;

    Server()throws Exception {
        String dir = System.getProperty("user.dir");
        htmlFile = new File("index.html");
        BufferedReader htmlFileReader;
        htmlFileReader = new BufferedReader(new FileReader(htmlFile));
        String str;
        String responseMessage = "";
        responseMessage += "HTTP/1.1 200 OK\r\n";

        String temp = "";
        while ((str = htmlFileReader.readLine()) != null) {

            str += "\n";
            temp += str;
        }

        responseMessage += "Content-Length: " + temp.length() + "\r\n";
        responseMessage += "Content-Type: text/html\r\n";
        responseMessage += "Connection: keep-alive\r\n";
        responseMessage += "\r\n";
        responseMessage += temp;

        System.out.println("FIRST RESPONSE MSG ---- > " + responseMessage);
        serverSocket = new ServerSocket(50000);
        while (true){
            try {

                System.out.println("before");
                socket = serverSocket.accept();

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                while (true) {
                    String receivedMessage = "";
                    while ((receivedMessage = in.readLine()) == null) {
                        System.out.println("-------------------------------------------------> no line");
                        socket = serverSocket.accept();
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                        in = new BufferedReader(new InputStreamReader(inputStream));
                    }


                    System.out.println("line 70 -------->" + receivedMessage);

                    String receivedMessageArray[] = receivedMessage.split(" ");


                    if (receivedMessageArray[0].equals("GET")) {

                        if (receivedMessage.equals("GET /index.html HTTP/1.1")) {

                            outputStream.write(responseMessage.getBytes());

                        } else {

                        }
                    } else if (receivedMessageArray[0].equals("POST")) {
                        String postMessage = "";
                        int contentLength = 0;
                        while (true) {
                            postMessage = in.readLine();
                            System.out.println("found ---> " + postMessage);
                            String parts[] = postMessage.split(" ");
                            if (parts[0].equals("Content-Length:")) {
                                contentLength = Integer.valueOf(parts[1]);
                                System.out.println("found content length = " + contentLength);
                            }
                            if (postMessage.equals("")) {
                                System.out.println("line break");
                                break;
                            }
                        }

                        byte b[] = new byte[contentLength + 1];
                        for (int i = 0; i < contentLength; i++) {
                            b[i] = (byte) in.read();
                            System.out.print((char) b[i]);
                        }
                        b[contentLength] = '&';
                        System.out.println("PURA b[] te ki ache dekhi nicer line e---------- >");

                        String s = new String(b);
                        System.out.println(s);

                        System.out.println("\n");

                        String data[] = postMessageExtract(b);
                        System.out.println("data[] " + data[0]);

                        String responseMessage2 = "";
                        responseMessage2 += "HTTP/1.1 200 OK\r\n";
                        responseMessage2 += "Content-Length: " + temp.length() + "\r\n";
                        responseMessage2 += "Content-Type: text/html\r\n";
                        responseMessage2 += "Connection: keep-alive\r\n";
                        responseMessage2 += "\r\n";
                        String temp2 = "<!DOCTYPE html>\n" +
                                "<html lang=\"en\">\n" +
                                "<head>\n" +
                                "    <meta charset=\"UTF-8\">\n" +
                                "    <title>new page</title>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "inputed data " + data[0] + "    " + data[1] +
                                "</body>\n" +
                                "</html>\n";
                        responseMessage2 += temp2;

                        System.out.println("Response Message hereeeeeeeeee -------> ");
                        System.out.println(responseMessage2);

                        outputStream.write(responseMessage2.getBytes());

                        socket.close();

                        /*Socket bankSocket = new Socket("127.0.0.1",60000);
                        OutputStream bankSend = bankSocket.getOutputStream();
                        for(int i=0;i<6;i++)
                        {
                            bankSend.write((data[i]+"\n").getBytes());
                        }*/
                    }


                }


            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    String[] postMessageExtract(byte b[])
    {
        String temp[]=new String[10];

        int k=0;
        for(int i=0;i<b.length;i++)
        {
            if(b[i]=='=')
            {
                int j=i+1;
                for(;j<b.length;j++)
                {
                    if(b[j]=='&')
                    {
                        if(j==i+1)
                        {
                            temp[k]="";
                            k++;
                            break;
                        }
                        temp[k]=new String(b,i+1,j-1-i);
                        k++;
                        break;
                    }
                }
            }
        }

        String data[]=new String[k];
        for(int i=0;i<k;i++)
        {
            data[i]=temp[i];
        }
        return data;
    }

}
