import java.io.*;
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
}
