package multichat;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.net.ServerSocket;

public class MultiThreadChatServer {
//    static Vector<ClientHandler> ar = new Vector<>(); 
	Grouplist grouplist = new Grouplist();
	
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static Vector<Vector<String>> groups = new Vector<Vector<String>>();

    private static final int maxClientsCount = 5;
    private static final clientThread[] threads = new clientThread[maxClientsCount];

    public static void main(String args[]) {
    	 


      int portNumber = 2222;
      if (args.length < 1) {
        System.out.println("Usage: java MultiThreadChatServer <portNumber>\n"
                + "Now using port number=" + portNumber);
      }
      else {
        portNumber = Integer.valueOf(args[0]).intValue();
      }

      try {
        serverSocket = new ServerSocket(portNumber);    // cuz now u have the confirmed port nos converted to int if user has passed port no
      } 
      catch (IOException e) {
        System.out.println(e);
      }

      while (true) {  // cuz Server has to continuously keep listening to client ( accept() )


        try {

          clientSocket = serverSocket.accept();
          int i = 0;

          for (i = 0; i < maxClientsCount; i++) {
            if (threads[i] == null) {
            	DataInputStream is = new DataInputStream(clientSocket.getInputStream());
               PrintStream os = new PrintStream(clientSocket.getOutputStream());
               String name;
            	os.println("Client name.");       // prints to client console
               name = is.readLine().trim();
                os.println("Start Chat by name " + name);   // prints to client console
              (threads[i] = new clientThread(clientSocket, threads,name,groups)).start();    // cuz u will be creating multiple threads on 1 port 
                                                                                // only ie clientSocket
                                                                                // and arguments are (clientSocket, array of threads)
                                                                                // calling the constructor of clientThread class that we wrote
              break;        // on looping thru all threads in array once u get null thread take it for use n break 
            }
          }



          if (i == maxClientsCount) {   // after looping if u did not get any null thread means u have got no thread for use and
                                        // i equals to maxClientsCount
            PrintStream os = new PrintStream(clientSocket.getOutputStream());
            os.println("Server too busy. Try later.");
            os.close();
            clientSocket.close();
          }


        } 
        catch (IOException e) {
          System.out.println(e);
        }
      }
  }
}

class clientThread extends Thread {   // 1 of the methods to implement multi-threading outside pvsm class MultiThreadChatServer
Grouplist grouplist = new Grouplist();
  private DataInputStream is = null;    // these r local variables similar but same variables we created above and we r passing them while  
                                        // using the functions of Thread class...... so these r just formal parameters
  
  private PrintStream os = null;      // OutputStreams are meant for binary data. Writers (including PrintWriter) are meant for text data


  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private String name = null;
  String destination= null;
   int choice;

  public clientThread(Socket clientSocket, clientThread[] threads,String name,Vector<Vector<String>> groups) {    // constructor
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
    this.name=name;
   
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;     // again intitialiization ???
    clientThread[] threads = this.threads;

    try {
        is = new DataInputStream(clientSocket.getInputStream());
        os = new PrintStream(clientSocket.getOutputStream());
//        os.println("Client name.");       // prints to client console
//        String name = is.readLine().trim();
        os.println("Start Chat by name " + name);   // prints to client console

        for (int i = 0; i < maxClientsCount; i++) {   // for every client go to next line except the current one
          if (threads[i] != null && threads[i] != this) {
            threads[i].os.println("");
          }
        }

        while (true) {    // keep printing msgs to every client while chatting is true----------- so its kinda every msg while chatting  
                          // is 1st passed to Server then Server gives that msg to each client 1 by 1
        	String line = is.readLine();
        	os.println("Select:\n\t1.group chat\n2.personal chat");
        	choice = Integer.valueOf(is.readLine());
        	os.println("Enter destinations name");
        	destination = is.readLine();
        	int flag=0;
        	
          if (line.startsWith("/quit")) {   // that client wants to stop talking
            break;
          }
          
          
          if(choice==1) {//select group chat here;
        	  
        	  if(Grouplist.group.get(destination)!=null) {
          		Grouplist.group.get(destination).add(name);
          		flag=1;
          	}
        	  else if(flag==0) {
         		os.println("No such grp exists! Do you want to create a new group?\n1.YES\n2.NO");
          		int create = Integer.valueOf(is.readLine().trim());
          		if(create==1) {
          			Grouplist.group.put(destination,new ArrayList<String>(Arrays.asList(name)));//here goes list name
          		}
          	}
          	
          for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && Grouplist.group.get(destination).indexOf(threads[i].name)!=-1 &&(threads[i].name.equals(Grouplist.group.get(destination).get(Grouplist.group.get(destination).indexOf(threads[i].name))) || threads[i].name.equals(this.name))) {
              threads[i].os.println(name + ">" + line);
            }
          }
          
          
          }
          else if(choice==2) {
        	  for (int i = 0; i < maxClientsCount; i++) {
                  if (threads[i] != null && (threads[i].name.equals(this.destination) || threads[i].name.equals(this.name))) {
                    threads[i].os.println(name + ">" + line);
                  }
                }
          }
          

        }


        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this) {   // for every client go to next line except the current one
            threads[i].os.println(" ");
          }
        }
       os.println(" ");       // this prints to Server console which client wanna quit

        for (int i = 0; i < maxClientsCount; i++) {       // free the current thread
          if (threads[i] == this) {
            threads[i] = null;
          }
        }

        is.close();
        os.close();
        clientSocket.close();
    } 
    catch (IOException e) {}
  }
}
class Grouplist{
	public static  Map<String,List<String>> group = new HashMap<String,List<String>>();
 }
