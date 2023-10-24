import java.io.*;
import java.util.*;


public class PeerProcess {
    public static void main(String[] args) throws FileNotFoundException {

        // Check for peer ID when compiling
        if (args.length != 1) {
            System.out.println("Usage: java peerProcess [peerID]");
            System.exit(1);
        }

        // Set up peers
        int peerID = Integer.parseInt(args[0]);
        File peerInfo = new File("PeerInfo.cfg");
        Scanner input = new Scanner(peerInfo);
        HashMap<Integer, Peer> peers = new HashMap<Integer, Peer>();

        // Add peers from PeerInfo.cfg
        while (input.hasNextLine()) {
            Peer peer = new Peer();
            String line = input.nextLine();
            String[] peerVariables = line.split(" ");
            peer.setInfo(Integer.parseInt(peerVariables[0]), peerVariables[1], Integer.parseInt(peerVariables[2]), Integer.parseInt(peerVariables[3]));
            peers.put(peer.peerID, peer);
        }

        peers.get(peerID).setPeerManager(peers);
        peers.get(peerID).readFile();





        // Implement Peer Behavior
        // Handle TCP connections, message exchanges, choking/unchoking, file management, etc.

        // Implement Logging
//        String logEntry = "[Time]: Peer " + peerID + " is unchoked by [peer_ID]";
        // Write logEntry to the log file

        // Implement File Handling
        // Manage complete and partial files

        // Local Testing
        // Test your program locally

    }
}
