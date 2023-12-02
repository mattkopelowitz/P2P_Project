import java.io.*;
import java.util.*;
import java.net.Socket;
import java.nio.*;

// handles incoming messages with runnable
public class MessageHandler implements Runnable{

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Peer p;
    private Socket s;
    private Peer targetPeer;
    private Message message;

    // Constructor for Serv.java
    public MessageHandler(ObjectInputStream input, ObjectOutputStream output, Peer p) {
        this.input = input;
        this.output = output;
        this.p = p;
    }

    // Constructor for Cli.java
    public MessageHandler(ObjectInputStream input, ObjectOutputStream output, Peer p, Socket s, Peer target) {
        this.input = input;
        this.output = output;
        this.p = p;
        this.s = s;
        targetPeer = target;
    }

    // Constructor for Peer.java
    public MessageHandler() {

    }

    public void run(){
        //deal with handshake messages
        byte[] handshakeBytes = new byte[0];
        try {
            handshakeBytes = message.handshakeMsg(p.peerID);
        } catch (IOException e) {}

        //send handshake message
        p.send(handshakeBytes, output, targetPeer.peerID);

        //continuously
        while(true) {
            // read in message length & type
            int messageLength = 0;
            byte messageType = 0;
            try {
                messageLength = input.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                messageType = input.readByte();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // read in the payload
            byte[] payload = new byte[messageLength - 1];
            try {
                input.readFully(payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
                
            //make new logwriter
            LogWriter log = new LogWriter(p);

            switch (messageType) {
                case 0: // log choked
                    log.chokedByNeighbor(p.peerID, targetPeer.peerID);
                    break;
                case 1: // log unchoked, if peer has a necessary file, send request
                    log.unchokedByNeighbor(p.peerID, targetPeer.peerID);
                    //check if necessary file
                    break;
                case 2: // log interested
                    log.receivedInterestedMsg(p.peerID, targetPeer.peerID);
                    break;
                case 3: // log not interested
                    log.receivedUninterestedMsg(p.peerID, targetPeer.peerID);
                    break;
                case 4: // log peer have, send interested/ not interested
                    int havePiece = ByteBuffer.wrap(payload).getInt();
                    log.receivedHaveMsg(p.peerID, targetPeer.peerID, havePiece);
                    break;
                case 5: // save peer's bitfield, send interested/ not interested

                    break;
                case 6: // log request, send piece if unchoked

                    break;
                case 7: // take in piece, send have to peers

                    break;
            }
    
        }

        // Close resources
//        try {
//            s.close();
//            input.close();
//            output.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}
