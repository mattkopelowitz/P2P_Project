import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client 
{
    Socket peerSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;

    public Client() 
	{}

    void run() 
	{
        try 
		{
            // Connect to a peer server
            peerSocket = new Socket("peer-address", 6881); // Replace with the peer's address and port
            System.out.println("Connected to peer server");

            // Initialize input and output streams
            out = new ObjectOutputStream(peerSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(peerSocket.getInputStream());

            // Simulate communication with the peer server
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) 
			{
                System.out.print("Enter a message (e.g., handshake, choke, unchoke, have, piece): ");
                message = bufferedReader.readLine();

                if (message.equalsIgnoreCase("handshake")) 
				{
                    sendHandshake();
                } else if (message.equalsIgnoreCase("choke")) 
				{
                    sendChoke();
                } else if (message.equalsIgnoreCase("unchoke")) 
				{
                    sendUnchoke();
                } else if (message.equalsIgnoreCase("have")) 
				{
                    sendHave();
                } else if (message.equalsIgnoreCase("piece")) 
				{
                    sendPiece();
                } else 
				{
                    sendMessage(message);
                }

                String response = (String) in.readObject();
                System.out.println("Received message from the peer: " + response);
            }
        } catch (ConnectException e) 
		{
            System.err.println("Connection refused. Ensure the peer server is running.");
        } catch (ClassNotFoundException e) 
		{
            System.err.println("Class not found");
        } catch (UnknownHostException unknownHost) 
		{
            System.err.println("Unknown host");
        } catch (IOException ioException) 
		{
            ioException.printStackTrace();
        } finally 
		{
            // Close connections
            try 
			{
                in.close();
                out.close();
                peerSocket.close();
            } catch (IOException ioException) 
			{
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) 
	{
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    void sendHandshake() 
	{
        try 
		{
            // Simulate sending a handshake message
            out.writeObject("Handshake message");
            out.flush();
        } catch (IOException ioException) 
		{
            ioException.printStackTrace();
        }
    }

    void sendChoke() 
	{
        try 
		{
            // Simulate sending a choke message
            out.writeObject("Choke message");
            out.flush();
        } catch (IOException ioException) 
		{
            ioException.printStackTrace();
        }
    }

    void sendUnchoke() 
	{
        try 
		{
            // Simulate sending an unchoke message
            out.writeObject("Unchoke message");
            out.flush();
        } catch (IOException ioException) 
		{
            ioException.printStackTrace();
        }
    }

    void sendHave() 
	{
        try 
		{
            // Simulate sending a have message
            out.writeObject("Have message");
            out.flush();
        } catch (IOException ioException) 
		{
            ioException.printStackTrace();
        }
    }

    void sendPiece() 
	{
        try 
		{
            // Simulate sending a piece message
            out.writeObject("Piece message");
            out.flush();
        } catch (IOException ioException) 
		{
            ioException.printStackTrace();
        }
    }

    public static void main(String[] args) 
	{
        Client client = new Client();
        client.run();
    }
}
