import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogWriter {

    Peer peer;
    int peerID;
    Logger logger;
    FileHandler fh;

    public LogWriter(Peer p) {
        peer = p;
        peerID = peer.peerID;
        logger = Logger.getLogger(Integer.toString(peerID));
        String path = "log_peer_" + Integer.toString(peerID) + ".log";

        try {
            fh = new FileHandler(path);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String time() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd 'at' hh:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return format.format(date) + ": ";
    }
    public void setVars(int peerID, BitSet bitfield, String hostName, int port, int containsFile) {
        logger.info(time() + "Peer " + peerID + " has started its server and contains the following initial variables: ");
        logger.info("Bitfield: " + bitfield.toString());
        logger.info("Hostname: " + hostName);
        logger.info("Port: " + port);
        logger.info("Contains file: " + containsFile);
    }
    public void setCommonVars(int numOfPreferredNeighbors, int unchokingInterval, int optimisticUnchokingInterval, String downloadFileName, int fileSize, int pieceSize, int numPieces) {
        logger.info(time() + "Common peer variables are set: ");
        logger.info("numbOfPreferredNeighbors: " + numOfPreferredNeighbors);
        logger.info("unchoking interval: " + unchokingInterval);
        logger.info("Optimistically unchoking interval: " + optimisticUnchokingInterval);
        logger.info("Download file name: " + downloadFileName);
        logger.info("File size: " + fileSize);
        logger.info("Piece size: " + pieceSize);
        logger.info("Number of pieces: " + numPieces);
    }
    public void tcpToPeer(int peerID1, int peerID2) {
        logger.info(time() + "Peer " + peerID1 + " made a connection to Peer " + peerID2 + ".");
    }
    public void connectedFromPeer(int peerID1, int peerID2) {
        logger.info(time() + "Peer " + peerID1 + " is connected from Peer " + peerID2 + ".");
    }
    public void changeNeighbors(int peerID, int[] neighborIDList) {
        logger.info(time() + "Peer " + peerID + " has the preferred neighbors " + Arrays.toString(neighborIDList) + ".");
    }
    public void optimisticUnchoke(int peerID, int unchokedNeighborID) {
        logger.info(time() + "Peer " + peerID + " has the optimistically unchoked neighbor " + unchokedNeighborID + ".");
    }
    public void unchokedByNeighbor(int peerID1, int peerID2) {
        logger.info(time() + "Peer " + peerID1 + " is unchoked by " + peerID2 + ".");
    }
    public void chokedByNeighbor(int peerID1, int peerID2){
        logger.info(time() + "Peer " + peerID1 + " is choked by " + peerID2 + ".");
    }
    public void receivedHaveMsg(int peerID1, int peerID2, int pieceIndex) {
        logger.info(time() + "Peer " + peerID1 + " received the 'have' message from " + peerID2 + " for the piece " + pieceIndex + ".");
    }
    public void receivedInterestedMsg(int peerID1, int peerID2) {
        logger.info(time() + "Peer " + peerID1 + " received the 'interested' message from " + peerID2 + ".");
    }
    public void receivedUninterestedMsg(int peerID1, int peerID2) {
        logger.info(time() + "Peer " + peerID1 + " received the 'not interested' message from " + peerID2 + ".");
    }
    public void receivedRequestMsg(int peerID1, int peerID2, int pieceIndex) {
        logger.info(time() + "Peer " + peerID1 + " has received the 'request' message for piece " + pieceIndex + " from Peer " + peerID2 + ".");
    }
    public void downloadedPiece(int peerID1, int peerID2, int pieceIndex, int numPieces) {
        logger.info(time() + "Peer " + peerID1 + " has downloaded the piece " + pieceIndex + " from " + peerID2 + ". Now the number of pieces it has is " + numPieces + ".");
    }
    public void downloadComplete(int peerID) {
        logger.info(time() + "Peer " + peerID + " has downloaded the file.");
    }
}
