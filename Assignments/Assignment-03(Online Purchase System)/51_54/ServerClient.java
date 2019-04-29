import java.io.*;
import java.net.Socket;

public class ServerClient {

    public String b_ip="";
    public int b_port;
    public String fName="",familyName="",postCode="",cardNumber="",quantity;



    //initializing the constructor.

    public ServerClient(String ip, int port, String nam, String fmly, String pst, String crd, String qntty){
        b_ip=ip;
        b_port=port;
        fName=nam;
        familyName=fmly;
        postCode=pst;
        cardNumber=crd;
        quantity=qntty;
    }


    //Here we want to get the message after submitting the purchase form on the described 3 terms.

    public String callBankServer(){
                       String msg="";

                        try{
                            Socket bank_socket = new Socket("127.0.0.1",b_port);

                            DataInputStream datain = new DataInputStream((bank_socket.getInputStream()));
                            DataOutputStream dataout = new DataOutputStream(bank_socket.getOutputStream());
                            BufferedReader bufferr = new BufferedReader(new InputStreamReader(System.in));



                            String to_bank_str=fName+"#"+familyName+"#"+postCode+"#"+cardNumber+"#"+quantity;
                            //System.out.println(to_bank_str);

                            dataout.writeUTF(to_bank_str);

                            //Here we get the message after submitting the purchase form on the described 3 terms.
                            msg=datain.readUTF();
                            System.out.println(msg);

                            datain.close();
                            dataout.close();
                            bank_socket.close();



                        }catch (IOException e){

                        }

                        return msg;

    }

}
