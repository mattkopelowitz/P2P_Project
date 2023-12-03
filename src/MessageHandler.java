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
                p.send(message.bitFieldMsg(p.bitfield), output, remotePeerID);
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
                        BitSet missingPieces = (BitSet) targetPeer.bitfield.clone();
                        missingPieces.andNot(p.bitfield);

                        Random random = new Random();
                        int index = missingPieces.nextSetBit(random.nextInt(missingPieces.length()));

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
                    int haveIndex = ByteBuffer.wrap(payload).getInt();
                    log.receivedHaveMsg(p.peerID, targetPeer.peerID, haveIndex);
                    // update remote peer's bitfield
                    Peer remote = p.peerManager.get(remotePeerID);
                    remote.bitfield.set(haveIndex, true);
                    if(remote.bitfield.nextClearBit(0) == remote.numPieces) {
                        remote.hasFile = true;
                        remote.containsFile = 1;
                    }
                    BitSet remoteBitfield = remote.bitfield;

                    //if peer bitfield and updated bitfield match, send not interested
                    if (p.bitfield.equals(remoteBitfield) || (remoteBitfield.isEmpty() && p.bitfield.isEmpty())) {
                        try {
                            p.send(message.notInterestedMsg(), output, remotePeerID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (!remoteBitfield.isEmpty() && p.bitfield.isEmpty()){
                        //send interested message
                        try {
                            p.send(message.interestedMsg(), output, remotePeerID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        
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
                    } else { // peer bitfield is not empty and is not equal to remote
                        BitSet interesting = (BitSet) p.bitfield.clone();
                        interesting.xor(remoteBitfield);

                        //send interested or not
                        if (!interesting.isEmpty()) {
                            try {
                                p.send(message.interestedMsg(), output, remotePeerID);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                p.send(message.notInterestedMsg(), output, remotePeerID);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // update peer's interesting pieces map
                        if(p.interestingPieces.containsKey(remotePeerID)) {
                            if (!interesting.isEmpty()) {
                                p.interestingPieces.replace(remotePeerID, interesting);
                            } else { 
                                p.interestingPieces.remove(remotePeerID);
                            }
                        } else {
                            p.interestingPieces.put(remotePeerID, interesting);
                        }
                    }

                    break;
                case 5: // save peer's bitfield, send interested/ not interested
                    // setting peer bitfield
                    BitSet recieved = BitSet.valueOf(payload);
                    p.peerManager.get(remotePeerID).bitfield = (BitSet) recieved.clone();

                    // set & send interested/ not interested
                    if (p.bitfield.equals(recieved) || (p.bitfield.isEmpty() && recieved.isEmpty())) {
                        try {
                            p.send(message.notInterestedMsg(), output, remotePeerID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (!recieved.isEmpty() && p.bitfield.isEmpty()) {
                        //send interested message
                        try {
                            p.send(message.interestedMsg(), output, remotePeerID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        
                        //update interesting bits
                        BitSet interesting = (BitSet) p.bitfield.clone();
                        interesting.or(recieved);
                        
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
                        interesting.xor(recieved);

                        //send interested or not
                        if (!interesting.isEmpty()) {
                            try {
                                p.send(message.interestedMsg(), output, remotePeerID);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                p.send(message.notInterestedMsg(), output, remotePeerID);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // update peer's interesting pieces map
                        if(p.interestingPieces.containsKey(remotePeerID)) {
                            if (!interesting.isEmpty()) {
                                p.interestingPieces.replace(remotePeerID, interesting);
                            } else { 
                                p.interestingPieces.remove(remotePeerID);
                            }
                        } else {
                            p.interestingPieces.put(remotePeerID, interesting);
                        }
                    }

                    break;
                case 6: // log request, send piece if unchoked
                    // Extract the piece index from the payload
                    int requestedPieceIndex = ByteBuffer.wrap(payload).getInt();

                    // Check if the peer is unchoked
                    if(!p.unchokedPeers.contains(targetPeer)) {
                        break;
                    }

                    if (p.bitfield.get(requestedPieceIndex)) {

                        // If unchoked and has the piece, send the piece to the requesting peer
                        byte[] pieceData = p.file[requestedPieceIndex].clone();

                        try {
                            p.send(message.pieceMsg(requestedPieceIndex, pieceData), output, remotePeerID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 7: // take in piece, send have to peers

                    break;
            }
    
        }

    }
}
