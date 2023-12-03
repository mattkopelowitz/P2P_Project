import java.io.*;
import java.net.Socket;

public class Client {
    private Peer peer;
    private Peer targetPeer;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Takes host peer and target peer
    public Client(Peer peer, Peer targetPeer) {
        this.peer = peer;
        this.targetPeer = targetPeer;
    }

    // Link a peer and a target peer
    public void link() {
        try (Socket socket = new Socket(targetPeer.hostName, targetPeer.portNumber);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Create message handler to handle incoming and outgoing messages
            MessageHandler handler = new MessageHandler(in, out, peer);

            // Start the handler on its own thread
            Thread handlerThread = new Thread(handler);
            handlerThread.start();

        } catch (IOException ioException) {
            System.err.println("IOException during connection:");
            ioException.printStackTrace();
        }
    }
}
