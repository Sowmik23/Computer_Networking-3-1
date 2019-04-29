import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class bank {
    public static void main(String [] args) throws Exception{
        try{
            //here taking input with the same time when we run the java file
            String b_port=args[0];

            //Converting the string input into integer
            int port=Integer.parseInt(b_port);
            ServerSocket serverSocket = new ServerSocket(port);

            // Taking null initially for exception handling
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream;
            BufferedReader bufferedReader;

            // Taking null initially for exception handling
            Socket socket = null;

            int looping=1000000;

            //This meaning The store verify the client in 1000000 times.
            while (looping!=0){
                looping--;
                socket = serverSocket.accept();
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(System.in));

                String str2="",Fname="",familyName="",postCode="",cardNo="",quantity="";

                str2=dataInputStream.readUTF();
                int totalcustomer=0;
                int invalidcustomer=0;
                boolean flag=false;
                String[] msArray = new String[50];


                //Here we get the purchase information

                StringTokenizer stk=new StringTokenizer(str2,"#");
                int cnt=0;
                while (stk.hasMoreTokens()){
                    if(cnt==0){
                        Fname=stk.nextToken();
                    }
                    if(cnt==1){
                        familyName=stk.nextToken();
                    }
                    if(cnt==2){
                        postCode=stk.nextToken();
                    }
                    if(cnt==3){
                        cardNo=stk.nextToken();
                    }
                    if(cnt==4){
                        quantity=stk.nextToken();
                    }
                    cnt++;
                }

                try{
                    FileReader frr=new FileReader("database.txt");
                    BufferedReader brr=new BufferedReader(frr);

                    String bankdata;
                    String b_name="",b_family_name="",b_post_code="",b_cardNo="",b_balance="",b_credit="";

                    while ((bankdata=brr.readLine())!=null){

                        int i=0;
                        msArray[totalcustomer]=bankdata;
                        StringTokenizer stk2=new StringTokenizer(bankdata,"#");

                        //Here we retrive data from the database.
                        while (stk2.hasMoreTokens()){
                            if(i==0){
                                b_name=stk2.nextToken();
                            }
                            if(i==1){
                                b_family_name=stk2.nextToken();
                            }
                            if(i==2){
                                b_post_code=stk2.nextToken();
                            }
                            if(i==3){
                                b_cardNo=stk2.nextToken();
                            }
                            if(i==4){
                                b_balance=stk2.nextToken();
                            }
                            if(i==5){
                                b_credit=stk2.nextToken();
                            }

                            i++;
                        }
                        totalcustomer++;
                        if(!Fname.equals(b_name) || !familyName.equals(b_family_name) || !postCode.equals(b_post_code) || !cardNo.equals(b_cardNo)){
                            invalidcustomer++;
                        }
                        else{
                            int Quantity=Integer.parseInt(quantity);
                            int total_bal=Integer.parseInt(b_balance);
                            int Credit=Integer.parseInt(b_credit);

                            if((Quantity*50)>Credit){
                                dataOutputStream.writeUTF("Your account does not have sufficient credit for the requested transaction");
                            }
                            else{
                                dataOutputStream.writeUTF("Transaction Approved");
                                flag=true;
                                int amnt=total_bal+(Quantity*50);
                                Credit=Credit-((Quantity*50));
                                msArray[totalcustomer-1]=b_name+"#"+b_family_name+"#"+b_post_code+"#"+b_cardNo+"#"+amnt+"#"+Credit;
                            }

                        }

                    }

                    frr.close();
                    brr.close();
                    if(totalcustomer==invalidcustomer){
                        dataOutputStream.writeUTF("The user information enterd is invalid");
                    }

                    //Here we update the database after any purchase.
                    if(flag==true){
                        FileWriter fw=new FileWriter("database.txt");
                        String fstr="";
                        for(int i=0;i<totalcustomer;i++){
                            fstr+=msArray[i]+"\n";
                        }
                        fw.write(fstr);
                        fw.close();
                    }
                    socket.close();
                }
                catch (FileNotFoundException e){

                }


            }


            dataOutputStream.close();
            socket.close();

        }catch (Exception e){

        }

    }
}

