/*Σημείωση : Κατά το compile να χρησιμοποιηθεί κωδικοποίση UTF8 διότι τα σχόλια στον κώδικα είναι με ελληνικούς χαρακτήρες .
        Συγκεκριμένα : javac -encoding utf8 MailServer.java
        κι αντίστοιχα για τον Client.*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class MailClient {
    public static void main(String[] args)  {
        try {
            Scanner scn = new Scanner(System.in);

            //Παίρνουμε σαν όρισμα την IP διεύθυνση
            InetAddress ip = InetAddress.getByName(args[0]);

            //Δημιουργούμε το socket βάσει IP και Port
            Socket socket = new Socket(ip, Integer.parseInt(args[1]));


            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //Thread για αποστολή μηνυμάτων
            Thread sendMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String message = scn.nextLine();
                        try {

                            out.writeUTF(message);
                            if(message.equals("Exit")){
                                System.out.println("Closing this connection : " + socket);
                                socket.close();
                                System.out.println("Connection closed");
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            //Thread για αποδοχή μηνυμάτων
            Thread getMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        try {
                            String message = in.readUTF();
                            System.out.println(message);
                        } catch (IOException e) {
                            break;

                        }
                    }
                }
            });

            sendMessage.start();
            getMessage.start();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
