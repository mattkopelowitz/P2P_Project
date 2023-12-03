
import java.net.*;
import java.io.*;

public class OldServer
{
    private static final int sPort = 6881;

    public static void main(String[] args) throws Exception 
	{
        System.out.println("The BitTorrent server is running.");
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        try 
		{
            while (true) 
			{
                new Handler(listener.accept(), clientNum).start();
                System.out.println("Peer " + clientNum + " is connected!");
                clientNum++;
            }
        } 
		finally 
		{
            listener.close();
        }
    }

    /**
     * A handler thread class. Handlers are spawned from the listening loop and are responsible for dealing with a single peer's requests.
     */
    private static class Handler extends Thread 
	{
        private String message; // Message received from the peer
        private String response; // Response message to send to the peer
        private Socket connection;
        private ObjectInputStream in; // Stream read from the socket
        private ObjectOutputStream out; // Stream write to the socket
        private int peerId; // The index number of the peer

        public Handler(Socket connection, int peerId) 
		{
            this.connection = connection;
            this.peerId = peerId;
        }

        public void run() 
		{
            try 
			{
                // Initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());

                try 
				{
                    while (true) 
					{
                        // Receive the message sent from the peer
                        message = (String) in.readObject();
                        System.out.println("Received message: " + message + " from peer " + peerId);

                        // Process the message and generate a response
                        response = processMessage(message);

                        // Send the response back to the peer
                        sendMessage(response);
                    }
                } 
				catch (ClassNotFoundException classnot) 
				{
                    System.err.println("Data received in an unknown format");
                }

            } 
			catch (IOException ioException) 
			{
                System.out.println("Disconnected from Peer " + peerId);
            } 
			finally 
			{
                // Close connections
                try 
				{
                    in.close();
                    out.close();
                    connection.close();
                } 
				catch (IOException ioException) 
				{
                    System.out.println("Disconnected from Peer " + peerId);
                }
            }
        }

        // Simulate processing a message and generating a response
        public String processMessage(String msg) 
		{
            return "Response to " + msg;
        }

        // Send a message to the output stream
        public void sendMessage(String msg) 
		{
            try 
			{
                out.writeObject(msg);
                out.flush();
                System.out.println("Sent message: " + msg + " to Peer " + peerId);
            } 
			catch (IOException ioException) 
			{
                ioException.printStackTrace();
            }
        }
    }
}
