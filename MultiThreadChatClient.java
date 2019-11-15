import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiThreadChatClient implements Runnable {			// now here we use 2nd method for thread implementation

  private static Socket clientSocket = null;
  private static PrintStream os = null;
  private static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args) {

    int portNumber = 2222;
    String host = "localhost";

    if (args.length < 2) {
      System.out.println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
              + "Now using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));		// unlike Server now in Client side u have to take ip from keyboard
      os = new PrintStream(clientSocket.getOutputStream());				// writing to Server
      is = new DataInputStream(clientSocket.getInputStream());			// reading from Server
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    if (clientSocket != null && os != null && is != null) {
      try {

        new Thread(new MultiThreadChatClient()).start();		// create new thread
        while (!closed) {							
          os.println(inputLine.readLine().trim());				// send msg to server while client is sending msgs is true 
        }

        os.close();
        is.close();
        clientSocket.close();
      } 
      catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  public void run() {			// this function is outside psvm but inside MultiThreadChatClient class ------------------ note
    String responseLine;

    try {
      while ((responseLine = is.readLine()) != null) {	// this msg is from Server cuz 'is' is used for reading msg  
        System.out.println(responseLine);				// print msg to console of client who is sending msg also
        if (responseLine.indexOf("* Bye") != -1)
          break;
      }
      closed = true;
    } 
    catch (IOException e) {
      System.err.println("IOException:  " + e);
    }

  }
}