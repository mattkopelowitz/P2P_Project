import java.io.*;
import java.net.Socket;


//todo
//////////////////////////////////////////////////////////////////
//         THIS IS A TEST CLIENT.JAVA FILE                      //
//////////////////////////////////////////////////////////////////

public class TestClient {
    private Peer peer;
    private Peer targetPeer;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Takes host peer and target peer
    public TestClient(Peer peer, Peer targetPeer) {
        this.peer = peer;
        this.targetPeer = targetPeer;
    }

    // Link a peer and a target peer
    public void link() {
        try {
            Socket socket = new Socket(targetPeer.hostName, targetPeer.portNumber);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            // Create message handler to handle incoming and outgoing messages
            MessageHandler handler = new MessageHandler(in, out, peer, targetPeer);

            // Start the handler on its own thread
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
        } catch (IOException ioException) {
            System.err.println("IOException during connection:");
            ioException.printStackTrace();
        }
    }
}

