import java.io.*;
import java.util.*;
import java.net.Socket;
import java.nio.*;
import java.nio.charset.StandardCharsets;

// handles incoming messages with runnable
public class MessageHandler implements Runnable{

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Peer p;
    private Peer targetPeer;
    private Message message;

    // Constructor for Server.java
    public MessageHandler(ObjectInputStream input, ObjectOutputStream output, Peer p) {
        this.input = input;
        this.output = output;
        this.p = p;
    }

    // Constructor for Client.java
    public MessageHandler(ObjectInputStream input, ObjectOutputStream output, Peer p, Peer target) {
        this.input = input;
        this.output = output;
        this.p = p;
        targetPeer = target;
    }

    // Constructor for Peer.java
    public MessageHandler() {

    }

    public void run(){
        // get id int
        int remotePeerID = new Integer(targetPeer.peerID);

        //make new logwriter
        LogWriter log = new LogWriter(p);

        //deal with handshake messages
        byte[] handshakeBytes = new byte[0];
        try {
            handshakeBytes = message.handshakeMsg(p.peerID);
        } catch (IOException e) {}

        //send handshake message
        p.send(handshakeBytes, output, remotePeerID);
        
        //get the handshake message
        byte[] handshakeHeader = new byte[18];
        try {
            input.readFully(handshakeHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String handShakeStr = new String(handshakeHeader, StandardCharsets.UTF_8);

        //skip the zero bytes and get the peerID
        byte[] zeros = new byte[10];
        try {
            input.readFully(zeros);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] peerIdBytes = new byte[4];
        try {
            input.readFully(peerIdBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //once handshake is read in, check it and send bitfield
        if(handShakeStr.equals("P2PFILESHARINGPROJ")) {
            log.connectedFromAnotherPeer(p.peerID, targetPeer.peerID);
            try {
                p.send(Message.bitFieldMsg(p.bitfield), output, remotePeerID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                

            switch (messageType) {
                case 0: // log choked
                    log.chokedByNeighbor(p.peerID, targetPeer.peerID);
                    break;
                case 1: // log unchoked
                    log.unchokedByNeighbor(p.peerID, targetPeer.peerID);
                    //check if peer has file and request
                    if (!p.hasFile) {
                        // TODO get the index
                        int index = 0;

                        try {
                            p.send(message.requestMsg(index), output, remotePeerID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2: // log interested
                    log.receivedInterestedMsg(p.peerID, targetPeer.peerID);
                    // add interested peer
                    if(!p.interestedPeers.contains(targetPeer.peerID)){
                        p.interestedPeers.add(targetPeer.peerID);
                    }
                    break;
                case 3: // log not interested
                    log.receivedUninterestedMsg(p.peerID, targetPeer.peerID);
                    // remove interested peer
                    if(p.interestedPeers.contains(targetPeer.peerID)){
                        p.interestedPeers.remove(new Integer(targetPeer.peerID));
                    }
                    break;
                case 4: // log peer have, send interested/ not interested
                    int havePiece = ByteBuffer.wrap(payload).getInt();
                    log.receivedHaveMsg(p.peerID, targetPeer.peerID, havePiece);
                    // update peer's bitfield
                    //TODO

                    //if peer bitfield and updated bitfield match, send not interested
                    if (p.bitfield.equals(remoteBitfield) || remoteBitfield.isEmpty()) {
                        p.send(message.notInterestedMsg(), output, remotePeerID);
                    } else if (!remoteBitfield.isEmpty() && p.bitfield.isEmpty()){
                        //send interested message
                        p.send(message.interestedMsg(), output, remotePeerID);
                        
                        //update interesting bits
                        BitSet interesting = (BitSet) p.bitfield.clone();
                        interesting.or(remoteBitfield);
                        
                        if(p.interestingPieces.containsKey(remotePeerID)) {
                            if (!interesting.isEmpty()) {
                                p.interestingPieces.replace(remotePeerID, interesting);
                            } else { 
                                p.interestingPieces.remove(remotePeerID);
                            }
                        } else {
                            p.interestingPieces.put(remotePeerID, interesting);
                        }
                    } else {
                        BitSet interesting = (BitSet) p.bitfield.clone();
                        interesting.xor(remoteBitfield);
                        
                    }

                    break;
                case 5: // save peer's bitfield, send interested/ not interested

                    break;
                case 6: // log request, send piece if unchoked

                    break;
                case 7: // take in piece, send have to peers

                    break;
            }
    
        }

    }
}
