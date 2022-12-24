/*Σημείωση : Κατά το compile να χρησιμοποιηθεί κωδικοποίση UTF8 διότι τα σχόλια στον κώδικα είναι με ελληνικούς χαρακτήρες .
        Συγκεκριμένα : javac -encoding utf8 MailServer.java
        κι αντίστοιχα για τον Client.*/



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;




public class MailServer {



      static ArrayList<Account> users = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));

        Account account1 = new Account("ioannis","1234");
        Account account2 = new Account("ntouvelos","1234");
        users.add(account1);
        users.add(account2);
        Email e1= new Email("ioannis" , "ntouvelos" , "hello  " , "How are u doing this fine day ?");
        Email e2= new Email("ioannis" , "ntouvelos" , "Help" , "I would like to ask your help about this science project that we were talking about !");
        Email e3= new Email("ioannis" , "ntouvelos" , "Meeting  " , "Will you be able to attend today's meeting at 9 ?");
        Email e4= new Email("ntouvelos" , "ioannis" , "Covid " , "I hope you and your family are okay during this pandemic !");
        Email e5= new Email("ntouvelos" , "ioannis" , "Happy new year" , "Wish you the best for the year coming ! ");
        Email e6= new Email("ntouvelos" , "ioannis" , "Chess " , "Magnus Carlsen really owned the field yesterday !!");
        account2.mailbox.add(e1);
        account2.mailbox.add(e2);
        account2.mailbox.add(e3);
        account1.mailbox.add(e4);
        account1.mailbox.add(e5);
        account1.mailbox.add(e6);

        while(true){
            Socket socket = null;
            try{
                socket=serverSocket.accept();
                System.out.println("A new client connected !" + socket);

                DataInputStream in= new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                ClientHandler clientHandler= new ClientHandler(socket,in,out);

                Thread thread= new Thread(clientHandler);
                thread.start();


            }
            catch(Exception e) {
                socket.close();
                e.printStackTrace();
            }



        }
    }
}


// Κλάση που διαχειρίζεται όλες τις λειτουργίες του κάθε thread
class ClientHandler  implements Runnable
{

    final private DataInputStream in ;
    final private DataOutputStream out ;
    final  private Socket socket;




    ClientHandler(Socket socket, DataInputStream in, DataOutputStream out)
    {
        this.socket = socket;
        this.in= in;
        this.out=out;

    }
    //Συνάρτηση για μορφοποίηση της εξόδου στην εμφάνιση των emails , προσθέτουμε num κενά μετά απο το String str
    private static String spacesAfterString(String str, int num) {
    return String.format("%1$-" + num + "s", str);
}


    //Συνάρτηση που υλοποίει την λειτουργία εισόδου , ελέγχεται αν υπάρχει ο χρήστης , κι αν ναι αν ο κωδικός που δώθηκε είναι σωστός
    private String logIn(String username, String password) {

        String result= "Username not found ! Try again";
        for(int i=0;i<MailServer.users.size();i++){
            if(username.equals(MailServer.users.get(i).username)){
                if(password.equals(MailServer.users.get(i).password)){

                    result=("Welcome back " + username);
                    break;

                }
                else {
                    result=("Wrong password ! Try again  ");
                    break;
                }
            }

        }


        return result;
    }

    //Συνάρτηση που υλοποίει την λειτουργία εγγραφής , ελέγχεται αν υπάρχει ήδη χρήστης με το ίδιο username , αν όχι δημιουργείται ο λογαριασμός.
    //Επιστρέφει 1 αν ο λογαριασμός δημιουργήθηκε επιτυχώς ,αλλιώς -1
    private int register(String username, String password) {

                for(int i=0;i<MailServer.users.size();i++){
                    if(username.equals(MailServer.users.get(i).username)){
                        return -1;
                    }

                }
                Account account = new Account(username,password);
                MailServer.users.add(account);
                return 1;

    }
    //Συνάρτηση που υλοποίει την λειτουργία αποστολής email , ελέγχεται αν υπάρχει χρήστης με το username του receiver και αν ναι δημιουργείται το email και προστίθεται στο mailbox του receiver.
    //Επιστρέφει 1 αν το email στάλθηκε επιτυχώς ,αλλιώς -1
    private int newEmail(String sender, String receiver, String subject, String mainbody){
        for(int i=0;i<MailServer.users.size();i++){
            if(receiver.equals(MailServer.users.get(i).username)){
                 Email email = new Email(sender, receiver, subject, mainbody);
                 MailServer.users.get(i).mailbox.add(email);
                 return 1;
            }

        }
        return -1;
    }
    //Συνάρτηση που υλοποίει την λειτουργία εμφάνισης των email
    private void showEmails(String user) throws IOException{
        int userindex=0;
        int j=0;
        String status;
        for(int i=0;i<MailServer.users.size();i++){
            if(user.equals(MailServer.users.get(i).username)){
                userindex=i;
                break;
            }
        }
        String output1=spacesAfterString("Id",20)+spacesAfterString("From",40)+"Subject";
        out.writeUTF(output1);
        for(Email e : MailServer.users.get(userindex).mailbox){
            if(e.isNew){
                 status = "[New]";
            }
            else {
                 status= "     ";
            }

            String output2=spacesAfterString(String.valueOf(j+1),10)+spacesAfterString(status,10)+spacesAfterString(e.sender,40)+e.subject;
            out.writeUTF(output2 );
            j++;
        }
    }

    //Συνάρτηση που υλοποίει την λειτουργία διαβάσματος συγκεκριμένου email
    private void readEmail(String user, int id) throws IOException {


        for(int i=0;i<MailServer.users.size();i++){
            if(user.equals(MailServer.users.get(i).username)){
                if(id<0 || id >= MailServer.users.get(i).mailbox.size()){// Έλεγχος ύπαρξης συγκεκριμένου email
                    serverMessage();
                    out.writeUTF("Wrong id ! ");
                    return;
                }
                serverMessage();
                out.writeUTF(MailServer.users.get(i).mailbox.get(id).mainbody);
                if(MailServer.users.get(i).mailbox.get(id).isNew){
                    MailServer.users.get(i).mailbox.get(id).isNew=false; // Αλλάζουμε την κατάσταση σε διαβασμένο
                }
                return;
            }
        }

    }

    //Συνάρτηση που εμφανίζει το μήνυμα MailServer κάθε φορά που στέλνεται μήνυμα απο τον server στον client
    private void serverMessage() throws IOException {
        out.writeUTF("\n-------------\n" +
                "Mail Server : \n" +
                "-------------\n");
    }

    //Συνάρτηση που διαγράφει συγκεκριμένο email
    private void deleteEmail(String user, int id) throws IOException {
        for(int i=0;i<MailServer.users.size();i++){
            if(user.equals(MailServer.users.get(i).username)){
                if(id<0 || id >= MailServer.users.get(i).mailbox.size()){ // Έλεγχος ύπαρξης συγκεκριμένου email
                    serverMessage();
                    out.writeUTF("Wrong id ! ");
                    return;
                }
                MailServer.users.get(i).mailbox.remove(id);
                serverMessage();
                out.writeUTF("Email deleted successfully");
                return;
            }
        }
    }

    //Συνάρτηση που εμφανίζει μήνυμα αποσύνδεσης , η υλοποίηση γίνεται στην ουσία με έξοδο απο το δευτερεύων μενού (secondmenufunction)
    private void logout() throws IOException {
        out.writeUTF("You have been disconnected");
    }


    // Συνάρτηση που υλοποιεί την έξοδο του πελάτη , απελευθερώνει και τους πόρους που χρησιμοποιήθηκαν
    private void exit() throws  IOException{
        System.out.println("Client " + this.socket + " sends exit...");
        System.out.println("Closing this connection.");
        this.socket.close();
        System.out.println("Connection closed");
    }

    //Συνάρτηση που υλοποιεί το βασικό μενού του χρήστη .
    private void mainmenufunction(){
        String received;
        String username;
        String password;
        String toreturn;
        String result;
        boolean flag1=true;
        int j=0;

        while (flag1)
        {
            try {

                serverMessage();


                out.writeUTF("Hello you are connected as a guest ! \n"+
                                  "Type what  you want : \n" +
                                  "LogIn \n" +
                                  "SignIn  \n" +
                                  "Exit \n" );


                received = in.readUTF();

                //Περίπτωση εξόδου
                if(received.equals("Exit"))
                {
                    exit();
                    break;
                }

                serverMessage();


                switch (received) {


                    case "LogIn":

                        out.writeUTF("Type your username  ! ");
                        username = in.readUTF();
                        serverMessage();
                        out.writeUTF("Type your password  ! ");
                        password = in.readUTF();
                        result=logIn(username,password);
                        serverMessage();
                        out.writeUTF(result);
                        if(result.equals("Welcome back " + username)){
                            flag1=secondmenufunction(username);//Μόλις συνδεθεί ο χρήστης παιρνάμε στο δευτερεύων μενού.
                        }


                        break;

                    case "SignIn" :
                        do {
                            if(j>0){
                                serverMessage();
                                out.writeUTF("This username is already being used ! Please try again\n ");
                            }
                            j++;
                            out.writeUTF("Type your username  ! ");
                            username = in.readUTF();
                            serverMessage();
                            out.writeUTF("Type your password  ! ");
                            password = in.readUTF();

                        } while(register(username,password)==-1);

                        serverMessage();
                        out.writeUTF("You account was successfully created ! Please LogIn from the main menu ! ");
                        j=0;
                        break;

                    default:
                        serverMessage();
                        out.writeUTF("Invalid input");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //Συνάρτηση που υλοποιεί το δευτερεύων μενού του χρήστη (σε περίπτωση που συνδεθεί.)
    //Επιστρέφει true σε περίπτωση που κάνει logout o πελάτης ώστε να επιστρέψει στο αρχικό μενού και false σε περίπτωση που θέλει να τερματίσει την σύνδεση .
    private boolean secondmenufunction(String user){

        String received;
        String receiver ;
        String subject ;
        String mainbody;
        String id;



        while (true){
            try{
                out.writeUTF("\n=============\n" +
                                 "> NewEmail \n"+
                                 "> ShowEmails \n" +
                                 "> ReadEmail\n" +
                                 "> DeleteEmail  \n" +
                                 "> Logout \n" +
                                 "> Exit \n" +
                                 "=============\n");

                received=in.readUTF();
                switch (received){
                    case "NewEmail" :
                        serverMessage();
                        out.writeUTF("Receiver :");
                        receiver=in.readUTF();
                        serverMessage();
                        out.writeUTF("Subject :");
                        subject=in.readUTF();
                        serverMessage();
                        out.writeUTF("Main body : ");
                        mainbody=in.readUTF();
                        if(newEmail(user,receiver,subject,mainbody)==1){
                            serverMessage();
                            out.writeUTF("Mail sent successfully ! \n");
                        }
                        else {
                            serverMessage();
                            out.writeUTF("Receiver was not found ! \n");
                        }
                        break;
                    case "ShowEmails" :
                        serverMessage();
                        showEmails(user);
                        break;
                    case "ReadEmail" :
                            serverMessage();
                            out.writeUTF("Give id of email you want to read : ");
                            id=in.readUTF();
                            readEmail(user,Integer.parseInt(id)-1);
                            break;
                    case "DeleteEmail" :
                            serverMessage();
                            out.writeUTF("Give id of emai you want to delete : ");
                            id=in.readUTF();
                            deleteEmail(user,Integer.parseInt(id)-1);
                            break;
                    case "Logout" :
                        serverMessage();
                        logout();
                        return true;
                    case "Exit" :
                        exit();
                        return false;

                    default:
                        serverMessage();
                        out.writeUTF("Wrong input , try again ! ");

                }
            }
            catch (IOException e) {


                break;
            }


        }
        return false;
    }

    @Override
    // Κατά την εκκίνηση του Thread τρέχουμε την συνάρτηση για το βασικό μενού.
    public void run()
    {


            mainmenufunction();



        try
        {
            // closing resources
            this.in.close();
            this.out.close();

        }catch(IOException e){

            e.printStackTrace();
        }
    }
}


class Email {
    boolean isNew ;
    String sender;
    String receiver;
    String subject ;
    String mainbody;

        public Email (String sender , String receiver , String subject , String mainbody){
            this.sender=sender;
            this.receiver=receiver;
            this.mainbody=mainbody;
            this.subject=subject;
            isNew=true;
        }




}



class Account {

    String username;
    String password;
    ArrayList<Email> mailbox = new ArrayList<>();

    public Account (String username , String password ){

        this.username=username;
        this.password=password;

    }






}