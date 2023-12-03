import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private Peer peer;

    public Server(Peer p) {
        peer = p;
    }

    public void run() {
        System.out.println("The BitTorrent server is running.");
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(peer.portNumber);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int clientNum = 1;

        try {
            while (true) {
                Socket connection = listener.accept();
                System.out.println("Peer " + clientNum + " is connected!");

                // Create a MessageHandler for the connected peer
                MessageHandler handler = new MessageHandler(new ObjectInputStream(connection.getInputStream()), new ObjectOutputStream(connection.getOutputStream()), peer);

                // Start the handler on a new thread
                Thread handlerThread = new Thread(handler);
                handlerThread.start();

                clientNum++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

