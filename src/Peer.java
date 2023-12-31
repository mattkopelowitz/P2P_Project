import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class Peer {

    // Peer info variables from PeerInfo.cfg
    int peerID;
    String hostName;
    int portNumber;
    int containsFile;
    HashMap<Integer, Peer> peerManager;
    List<Integer> interestedPeers;
    HashMap<Integer, BitSet> interestingPieces;
    byte[][] file;
    int downloadedBytes;
    double downloadRate = 0;
    ObjectOutputStream out;
    private Message message;
    int optUnchockedPeer = 0;
    List<Integer> unchokedPeers;

    // Common info variables from Common.cfg
    int numOfPreferredNeighbors;
    String downloadFileName;
    int fileSize;
    int pieceSize;
    int numPieces;
    BitSet bitfield;
    int numPiecesDownloaded;
    boolean hasFile;
    int unchokeInterval;
    int optUnchokeInterval;
    LogWriter log;

    // Peer Constructor
    public Peer() {
        try {
            readCommonCfg();
        } catch(FileNotFoundException e) {

        }
    }

    public void setInfo(int id, String name, int port, int conFile) {
        peerID = id;
        hostName = name;
        portNumber = port;
        containsFile = conFile;
        peerManager = new HashMap<Integer, Peer>();
        interestingPieces = new HashMap<Integer, BitSet>();
        interestedPeers = new ArrayList<>();
        bitfield = new BitSet(numPieces);
        message = new Message();
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

    void readCommonCfg() throws FileNotFoundException {
        File common = new File("Common.cfg");
        List<String> vars = new ArrayList<>();

        Scanner input = new Scanner(common);

        while (input.hasNextLine()) {
            String line = input.nextLine();
            String[] variables = line.split(" ");
            vars.add(variables[1]);
        }

        numOfPreferredNeighbors = Integer.parseInt(vars.get(0));
        downloadFileName = vars.get(3);
        fileSize = Integer.parseInt(vars.get(4));
        pieceSize = Integer.parseInt(vars.get(5));
        numPieces = (int) Math.ceil((double)fileSize/pieceSize);
        unchokeInterval = Integer.parseInt(vars.get(1)) * 1000;
        optUnchokeInterval = Integer.parseInt(vars.get(2)) * 1000;
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

    public void setPeerManager(HashMap<Integer, Peer> peerMan){
        peerManager = peerMan;
        log = new LogWriter(peerManager.get(peerID));
    }

    public void resetDownloadBytes() {
        this.downloadedBytes = 0;
    }
    public void addUnchoked(int peerID) {
        unchokedPeers.add(peerID);
    }

    public int getPeerID() {
        return peerID;
    }


    public void send(byte[] msg, ObjectOutputStream stream) {
        try {
            stream.writeObject(msg);
            stream.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void rmUnchoked(int peerID) {
        if (unchokedPeers.contains(peerID)) {
            unchokedPeers.remove(peerID);
        }
    }

    public static <K, V> K getKey(HashMap<K, V> map, V value) {
        return map.keySet().stream().filter(key -> value.equals(map.get(key))).findAny().get();
    }

    public List<Integer> getInterestedPeers() {
        return interestedPeers;
    }

    public void incrementPiecesDownloaded() {
        numPiecesDownloaded++;
    }

    public void updateBitfield(int index) {
        bitfield.set(index, true);
        if (bitfield.nextClearBit(0) == numPieces) {
            hasFile = true;
            containsFile = 1;
        }
    }

    public int getRequestIndex(int targetID) {
        BitSet pieces =  (BitSet)bitfield.clone();
        pieces.flip(0,numPieces);
        pieces.and(interestingPieces.get(targetID));
        List<Integer> index = new ArrayList<>();

        for(int i = 0; i < pieces.length(); i++){
            if(pieces.get(i) == true) index.add(i);
        }

        Collections.shuffle(index, new Random());
        return index.get(0);
    }

    public void saveFile() {
        FileOutputStream output = null;
        try {
            File path = new File("./peer_" + peerID);
            path.mkdirs();
            File location = new File(path, downloadFileName);
            location.createNewFile();
            output = new FileOutputStream(location);

            for(int i = 0; i < numPieces; i++){
                output.write(file[i]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if(output != null) {
                try {
                    output.flush();
                    output.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
