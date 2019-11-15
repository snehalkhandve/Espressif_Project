import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class MultiThreadChatServer {

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;

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
              (threads[i] = new clientThread(clientSocket, threads)).start();    // cuz u will be creating multiple threads on 1 port 
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

  private DataInputStream is = null;    // these r local variables similar but same variables we created above and we r passing them while  
                                        // using the functions of Thread class...... so these r just formal parameters
  
  private PrintStream os = null;      // OutputStreams are meant for binary data. Writers (including PrintWriter) are meant for text data


  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;

  public clientThread(Socket clientSocket, clientThread[] threads) {    // constructor
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;     // again intitialiization ???
    clientThread[] threads = this.threads;

    try {
        is = new DataInputStream(clientSocket.getInputStream());
        os = new PrintStream(clientSocket.getOutputStream());
        os.println("Client name.");       // prints to client console
        String name = is.readLine().trim();
        os.println("Start Chat by name " + name);   // prints to client console

        for (int i = 0; i < maxClientsCount; i++) {   // for every client go to next line except the current one
          if (threads[i] != null && threads[i] != this) {
            threads[i].os.println("");
          }
        }

        while (true) {    // keep printing msgs to every client while chatting is true----------- so its kinda every msg while chatting  
                          // is 1st passed to Server then Server gives that msg to each client 1 by 1
          String line = is.readLine();
          if (line.startsWith("/quit")) {   // that client wants to stop talking
            break;
          }

          for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null) {
              threads[i].os.println(name + ">" + line);
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
