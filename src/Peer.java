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
    double downloadRate;
    ObjectOutputStream out;
    private MessageHandler msgHandler;
    int optUnchockedPeer;
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

    public void setInfo(int id, String name, int port, int conFile) {
        peerID = id;
        hostName = name;
        portNumber = port;
        containsFile = conFile;
        peerManager = new HashMap<Integer, Peer>();
        interestingPieces = new HashMap<Integer, BitSet>();
        interestedPeers = new ArrayList<>();
        bitfield = new BitSet(numPieces);
        msgHandler = new MessageHandler();
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

    void readCommonCfg() throws FileNotFoundException {
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
        unchokeInterval = Integer.parseInt(vars.get(1)) * 1000;
        optUnchokeInterval = Integer.parseInt(vars.get(2)) * 1000;
    }

    public void chokeCounter() {
        Peer peer = this;
        Instant[] startTime = {Instant.now()};
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    peersSelection(interestedPeers, startTime[0]);
                    startTime[0] = Instant.now();
                    Thread.sleep(unchokeInterval);
                } catch (InterruptedException | IOException e) {
                    System.out.println("Interruption");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void resetDownloadBytes() {
        this.downloadedBytes = 0;
    }
    public void addUnchoked(int peerID) {
        unchokedPeers.add(peerID);
    }

    public void send(byte[] msg, ObjectOutputStream stream, int remotePeerID) {
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
    public void peersSelection(List<Integer> interestedPeers, Instant startTime) throws IOException {
        HashMap<Integer, Double> candidatePeers = new HashMap<>();
        int[] preferredNeighbors = new int[numOfPreferredNeighbors];
        List<Integer> peersToChoke = new ArrayList<>();
        Instant finish = Instant.now();

        peerManager.forEach((id, peerValues) -> {
            if (id != peerID) {
                int timeElapsed = Duration.between(startTime, finish).getNano();
                peerValues.downloadRate = ((double) peerValues.downloadedBytes / timeElapsed);
                candidatePeers.put(id, peerValues.downloadRate);
            }
        });

        resetDownloadBytes();

        Collection<Double> values = candidatePeers.values();
        ArrayList<Double> listOfValues = new ArrayList<>(values);
        listOfValues.sort(Comparator.naturalOrder());

        if (!interestedPeers.isEmpty()) {
            int peerSelectionLimit = Math.min(interestedPeers.size(), preferredNeighbors.length);

            for (int i = 0; i < peerSelectionLimit; i++) {
                preferredNeighbors[i] = interestedPeers.get(i);
            }

            log.changeNeighbors(peerID, preferredNeighbors);

            unchokedPeers.stream()
                    .filter(peer -> !Arrays.stream(preferredNeighbors).anyMatch(neighbor -> neighbor == peer && peer != 0))
                    .forEach(peer -> {
                        send(msgHandler.choke(), peerManager.get(peer).out, peer);
                        peersToChoke.add(peer);
                    });

            peersToChoke.forEach(this::rmUnchoked);
        } else if (containsFile == 1) {
            Collections.shuffle(interestedPeers, new Random());
            for (int i = 0; i < preferredNeighbors.length; i++) {
                preferredNeighbors[i] = interestedPeers.get(i);
            }

            log.changeNeighbors(peerID, preferredNeighbors);

            unchokedPeers.stream()
                    .filter(peer -> !Arrays.stream(preferredNeighbors).anyMatch(neighbor -> neighbor == peer && peer != 0))
                    .forEach(peer -> {
                        send(msgHandler.choke(), peerManager.get(peer).out, peer);
                        peersToChoke.add(peer);
                    });

            peersToChoke.forEach(this::rmUnchoked);
        } else {
            for (int i = 0; i < preferredNeighbors.length; i++) {
                if (interestedPeers.contains(getKey(candidatePeers, listOfValues.get(listOfValues.size() - 1)))) {
                    preferredNeighbors[i] = getKey(candidatePeers, listOfValues.get(listOfValues.size() - 1));
                }
                listOfValues.remove(listOfValues.size() - 1);
            }

            log.changeNeighbors(peerID, preferredNeighbors);

            Arrays.stream(preferredNeighbors)
                    .filter(neighbor -> !unchokedPeers.contains(neighbor) && neighbor != 0)
                    .forEach(neighbor -> {
                        addUnchoked(neighbor);
                        send(msgHandler.unchoke(), peerManager.get(neighbor).out, neighbor);
                    });

            unchokedPeers.stream()
                    .filter(peer -> !Arrays.stream(preferredNeighbors).anyMatch(neighbor -> neighbor == peer && peer != 0))
                    .forEach(peer -> {
                        send(msgHandler.choke(), peerManager.get(peer).out, peer);
                        peersToChoke.add(peer);
                    });

            peersToChoke.forEach(this::rmUnchoked);
        }
    }






    // BELOW NEEDS TO BE EDITED

    private List<Integer> getInterestedPeers() {
        return interestedPeers;
    }
    public void startOptUnchokePeer() {
        Peer peer = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                // Stop thread when all peers have finished downloading
                while(true) {
                    try {
                        peer.optUnchokePeer(peer.getInterestedPeers());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(optUnchokeInterval);
                    } catch (InterruptedException interruptedException) {
                        System.out.println("Thread to optimistically unchoke neighbor interrupted while trying to sleep.");
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void optUnchokePeer(List<Integer> InterestedPeers) throws IOException {
        List<Integer> candidatePeers = new ArrayList<>();
        for(int interestedPeerId : InterestedPeers) {
            if(!unchokedPeers.contains(interestedPeerId)) {
                candidatePeers.add(interestedPeerId);
            }
        }
        if(!candidatePeers.isEmpty()) {
            Collections.shuffle(candidatePeers, new Random());
            int optUnchokedPeerID = candidatePeers.get(0);
            log.optimisticUnchoke(peerID, optUnchokedPeerID);
            send(msgHandler.unchokeMsg(), peerManager.get(optUnchokedPeerID).out, optUnchokedPeerID);
            this.optUnchockedPeer = optUnchokedPeerID;
        }
    }


}
