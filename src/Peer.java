import java.io.*;
import java.util.*;
import java.nio.file.*;
public class Peer {

    // Peer info from PeerInfo.cfg
    int peerID;
    String hostName;
    int portNumber;
    int containsFile;
    HashMap<Integer, Peer> peerManager;
    List<Integer> interestedPeers;
    HashMap<Integer, BitSet> interestingPieces;
    byte[][] file;
    int downloadedBytes;
    double downloadRate;
    //ObjectOutputStream out;
    //private MessageCreator creator;
    //int optimisticallyUnchockedPeer;
    //List<Integer> unchokedPeers;

    // Common info from Common.cfg
    int numOfPreferredNeighbors;
    String downloadFileName;
    int fileSize;
    int pieceSize;
    int numPieces;
    BitSet bitfield;
    int numPiecesDownloaded;
    boolean hasFile;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    LogWriter log;

    public void setInfo(int id, String name, int port, int conFile) {
        peerID = id;
        hostName = name;
        portNumber = port;
        containsFile = conFile;
        peerManager = new HashMap<Integer, Peer>();
        interestingPieces = new HashMap<Integer, BitSet>();
        interestedPeers = new ArrayList<>();
        bitfield = new BitSet(numPieces);
        //creator = new MessageCreator();
        downloadedBytes = 0;
        numPiecesDownloaded = 0;

        if (containsFile == 1) {
            bitfield.set(0, numPieces, true);
            numPiecesDownloaded = numPieces;
            hasFile = true;
        } else {
            bitfield.clear(0, numPieces);
            hasFile = false;
        }

        file = new byte[numPieces][];
    }

    public void setPeerManager(HashMap<Integer, Peer> peerMan){
        peerManager = peerMan;
        //logger = new WritingLog(peerManager.get(peerID));
    }

    public void readFile() {
        if (containsFile == 1) {
            try {
                byte[] allBytes = Files.readAllBytes(Paths.get("./peer_" + peerID + "/" + downloadFileName));
                for (int i = 0, j = 0; i < allBytes.length; i += pieceSize, j++) {
                    byte[] bytes = Arrays.copyOfRange(allBytes, i, i + pieceSize);
                    file[j] = bytes;
                }
            } catch (IOException ioException) {}
        }
    }

    void readCommonCfg() throws FileNotFoundException{
        File common = new File("Common.cfg");
        List<String> vars = new ArrayList<>();

        Scanner input = new Scanner(common);

        while(input.hasNextLine()){
            String line = input.nextLine();
            String[] variables = line.split(" ");
            vars.add(variables[1]);
        }

        numOfPreferredNeighbors = Integer.parseInt(vars.get(0));
        downloadFileName = vars.get(3);
        fileSize = Integer.parseInt(vars.get(4));
        pieceSize = Integer.parseInt(vars.get(5));
        numPieces = (int) Math.ceil((double)fileSize/pieceSize);
        //unchokingInterval = Integer.parseInt(vars.get(1)) * 1000;
        //optimisticUnchokingInterval = Integer.parseInt(vars.get(2)) * 1000;
    }
}
