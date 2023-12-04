import java.io.*;
import java.util.*;


public class peerProcess {
    public static void main(String[] args) throws FileNotFoundException {

        // Check for peer ID when compiling
        if (args.length != 1) {
            System.out.println("Usage: java peerProcess [peerID]");
            System.exit(1);
        }

        // Set up map of peers
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

        // Set peer manager and reads the file to download
        peers.get(peerID).setPeerManager(peers);
        peers.get(peerID).readFile();

        // Start Logs
        LogWriter log = new LogWriter(peers.get(peerID));
        log.setVars(peerID, peers.get(peerID).bitfield, peers.get(peerID).hostName, peers.get(peerID).portNumber, peers.get(peerID).containsFile);
        log.setCommonVars(peers.get(peerID).numOfPreferredNeighbors, peers.get(peerID).unchokeInterval, peers.get(peerID).optUnchokeInterval, peers.get(peerID).downloadFileName, peers.get(peerID).fileSize, peers.get(peerID).pieceSize, peers.get(peerID).numPieces);

        // Start Server
        Server server = new Server(peers.get(peerID));
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Create Peer Clients
        Iterator iter = peers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry p = (Map.Entry)iter.next();

            if ((int)p.getKey() < peerID) {
                Client client = new Client(peers.get(peerID), (Peer)p.getValue());
                client.link();
                log.tcpToPeer(peerID, (int)p.getKey());
            }

//            peers.get(peerID).chokeCounter();
//            peers.get(peerID).optUnchokePeer();
        }

    }
}
