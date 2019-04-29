import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    Socket socket;
    ServerSocket serverSocket;

    File layoutFile;
    File file;

    Server()throws Exception {
        String dir = System.getProperty("user.dir");
        layoutFile = new File("index.html");
        BufferedReader htmlFileReader;
        htmlFileReader = new BufferedReader(new FileReader(layoutFile));
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

        System.out.println("1st response message---- > " + responseMessage);
        serverSocket = new ServerSocket(11111);	 //define port number 11111

        while (true){
            try {

                System.out.println("End of html txt");
                socket = serverSocket.accept();

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                while (true) {

                    String receivedMessage = "";
                    while ((receivedMessage = in.readLine()) == null) {
                        System.out.println("****************************> empty line");
                        socket = serverSocket.accept();
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                        in = new BufferedReader(new InputStreamReader(inputStream));
                    }


                    System.out.println("Received :	" + receivedMessage);

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
                            System.out.println("Found ---> " + postMessage);
                            String parts[] = postMessage.split(" ");
                            if (parts[0].equals("Content-Length:")) {
                                contentLength = Integer.valueOf(parts[1]);
                                System.out.println("Found Content Length = " + contentLength);
                            }
                            if (postMessage.equals("")) {
                                System.out.println("new line");
                                break;
                            }
                        }

                        byte arr[] = new byte[contentLength + 1];
                        for (int i = 0; i < contentLength; i++) {
                            arr[i] = (byte) in.read();
                            System.out.print((char) arr[i]);
                        }
                        arr[contentLength] = '&';
                        System.out.println("it will show the element of array arr[] ");

                        String s = new String(arr);
                        System.out.println(s);

                        System.out.println("\n");

                        String data[] = postMessageExtract(arr);
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
                                "    <title>feedback</title>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "inputed data " + data[0] + "    " + data[1] +  "    " + data[2] +  "    " + data[3] +  "    " + data[4] +  "    " + data[5] + 
                                "</body>\n" +
                                "</html>\n";
                        responseMessage2 += temp2;

                        System.out.println("response message is:  ");
                        System.out.println(responseMessage2);

                        outputStream.write(responseMessage2.getBytes());

                        socket.close();



                        /*Socket bankSocket = new Socket("localhost",22222);
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

    String[] postMessageExtract(byte arr[])
    {
        String temp[]=new String[10];

        int k=0;
        for(int i=0;i<arr.length;i++)
        {
            if(arr[i]=='=')
            {
                int j=i+1;
                for(;j<arr.length;j++)
                {
                    if(arr[j]=='&')
                    {
                        if(j==i+1)
                        {
                            temp[k]="";
                            k++;
                            break;
                        }
                        temp[k]=new String(arr,i+1,j-1-i);
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
