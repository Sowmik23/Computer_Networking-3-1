import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class store {

    public static void main(String[] args) throws Exception {
        try {
            Socket socket;
            ServerSocket serverSocket;

            File htmlFile;
            File file;
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


            String s_port,b_port;
            int server_port;
            int bank_port;
            String bank_ip = "";

            //here taking input with the same time when we run the java file
            s_port = args[0];
            bank_ip = args[1];
            b_port = args[2];

            //Converting the string input into integer
            server_port=Integer.parseInt(s_port);
            bank_port=Integer.parseInt(b_port);


            serverSocket = new ServerSocket(server_port);
            while (true) {
                try {


                    socket = serverSocket.accept();

                    InputStream inputStream = socket.getInputStream();
                    OutputStream outputStream = socket.getOutputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                    while (true) {
                        String receivedMessage = "";

                        //This loop checking no response state.
                        while ((receivedMessage = in.readLine()) == null) {

                            socket = serverSocket.accept();
                            inputStream = socket.getInputStream();
                            outputStream = socket.getOutputStream();
                            in = new BufferedReader(new InputStreamReader(inputStream));
                        }


                        String receivedMessageArray[] = receivedMessage.split(" ");


                        if (receivedMessageArray[0].equals("GET")) {

                            if (receivedMessage.equals("GET /index.html HTTP/1.1")) {

                                outputStream.write(responseMessage.getBytes());

                            }
                        }
                        else if (receivedMessageArray[0].equals("POST")) {
                            String postMessage = "";
                            int contentLength = 0;

                            //tracking the postmessage.
                            while (true) {
                                postMessage = in.readLine();

                                //partitions the post message corresponding to a blank space.
                                String parts[] = postMessage.split(" ");

                                //getting the length of the content.
                                if (parts[0].equals("Content-Length:")) {
                                    contentLength = Integer.valueOf(parts[1]);
                                }

                                //break the loop if it detects a newline.
                                if (postMessage.equals("")) {
                                    break;
                                }
                            }

                            byte b[] = new byte[contentLength + 1];

                            //Getting the client's CD purchase information.
                            for (int i = 0; i < contentLength; i++) {
                                b[i] = (byte) in.read();
                            }

                            //it detects the end of the content
                            b[contentLength] = '&';

                            String data[] = ExtractpostMessage(b);

                            //Setting up with data in the ServerClient constructor.
                            ServerClient sclient = new ServerClient(bank_ip, bank_port, data[0], data[1], data[2], data[3], data[5]);

                            ////Here we get the message after submitting the purchase form on the described 3 terms.
                            String message = sclient.callBankServer();


                            String responseMessage2 = "";
                            responseMessage2 += "HTTP/1.1 200 OK\r\n";
                            responseMessage2 += "Content-Length: " + temp.length() + "\r\n";
                            responseMessage2 += "Content-Type: text/html\r\n";
                            responseMessage2 += "Connection: keep-alive\r\n";
                            responseMessage2 += "\r\n";
                            String temp2 = "<html>\n" +
                                    "<head>\n" +
                                    "<title>Online CD Store</title>\n" +
                                    "</head>\n" +
                                    "<body>\n" +
                                    "<p>" + message + "</p>\n" +
                                    "</body>\n" +
                                    "</html>\n";
                            responseMessage2 += temp2;


                            outputStream.write(responseMessage2.getBytes());


                        }
                        socket.close();


                    }


                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] ExtractpostMessage(byte[] b) {
        String temp[] = new String[10];

        int k = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] == '=') {
                int j = i + 1;
                for (; j < b.length; j++) {
                    if (b[j] == '&') {
                        if (j == i + 1) {
                            temp[k] = "";
                            k++;
                            break;
                        }
                        temp[k] = new String(b, i + 1, j - 1 - i);
                        k++;
                        break;
                    }
                }
            }
        }

        String data[] = new String[k];
        for (int i = 0; i < k; i++) {
            data[i] = temp[i];
        }
        return data;
    }
}



