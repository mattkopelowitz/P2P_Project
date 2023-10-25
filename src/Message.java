import java.io.*;
import java.nio.ByteBuffer;
import java.util.BitSet;

// this class creates the byte array to be sent in each message 
public class Message {

    // constructor
    public Message(){
    };

    // creates handshake message
    public byte[] handshakeMessage(int peerID) throws IOException {

        // create output stream (makes buffer to write to)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

        // init handshake message string
        String handshakeStr = "P2PFILESHARINGPROJ";

        // write zero bits to output stream
        byte [] zeros = new byte[10];

        // write peer id to output stream 
        ByteBuffer peerIDBuff = ByteBuffer.allocate(4);
        peerIDBuff.putInt(peerID);
        byte[] peerIDByte = peerIDBuff.array();

        // write to output stream buffer
        outputStream.write(handshakeStr.getBytes());
        outputStream.write(zeros);
        outputStream.write(peerIDByte);

        outputStream.close();

        // return the message as a byte array
        return outputStream.toByteArray();
    }

    // creates choke message (value 0)
    public byte[] chokeMessage() throws IOException {

        // init output buffer and length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(5);

        // adds length value - 1 byte for the value 
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(1); 

        // sets type value to 0
        byte[] messageType = new byte[1];
        messageType[0] = 0;

        // write length and type - no payload for choke
        outputStream.write(lengthBuffer.array());
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }

    // create unchoke message (value 1)
    public byte[] unchokeMessage() throws IOException {

        // init output buffer & length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(5);

        // get message length 
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(1);

        // set value to 1
        byte[] messageType = new byte[1];
        messageType[0] = 1;


        // write length and value - no payload
        outputStream.write(lengthBuffer.array());
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }

    // create interested message (value 2)
    public byte[] interestedMessage() throws IOException {

        // init output buffer & length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(5);

        // get message length
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(1);

        // set value to 2
        byte[] messageType = new byte[1];
        messageType[0] = 2;

        // write length and value - no payload
        outputStream.write(lengthBuffer.array());
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }

    // create not interested message (value 3)
    public byte[] notInterestedMessage() throws IOException {

        // init output buffer & length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(5);

        // get length
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(1);

        // set value to 3
        byte[] messageType = new byte[1];
        messageType[0] = 3;

        // write length & value - no payload
        outputStream.write(lengthBuffer.array());
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }

    // create have message (value 4)
    public byte[] haveMessage(int pieceIndex) throws IOException{

        // init output buffer & length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;

        // set value to 4
        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 4;

        // add index - 4 byte payload gives index of the piece the sender has
        ByteBuffer pieceIndexBuffer = ByteBuffer.allocate(4);
        pieceIndexBuffer.putInt(pieceIndex);
        byte[] messagePayload = pieceIndexBuffer.array();
        lengthCount += messagePayload.length;

        // get length
        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        byte[] messageLength = messageLengthBuffer.array();

        // write length, type, payload
        outputStream.write(messageLength);
        outputStream.write(messageType);
        outputStream.write(messagePayload);

        return outputStream.toByteArray();
    }

    // creates bitfield message (value 5)
    public byte[] bitFieldMessage(BitSet bitfield) throws IOException {
        
        //create output stream & init length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;

        // sets message type to value 5 (indicates bitfield)
        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 5;

        // convert bitfield to byte array 
        byte[] messagePayload = bitfield.toByteArray();
        lengthCount += messagePayload.length;

        // gets the message length (stored in a 4 byte length field)
        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        byte[] messageLength = messageLengthBuffer.array();

        // write message to output stream
        outputStream.write(messageLength);
        outputStream.write(messageType);
        outputStream.write(messagePayload);

        outputStream.close();

        return outputStream.toByteArray();
    }

    // creates request message (value 6)
    public byte[] requestMessage(int pieceIndex) throws IOException{

        // init output buffer & length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;
    
        // set value to 6
        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 6;

        // set index - request a piece at a specific index
        ByteBuffer pieceIndexBuffer = ByteBuffer.allocate(4);
        pieceIndexBuffer.putInt(pieceIndex);
        byte[] messagePayload = pieceIndexBuffer.array();
        lengthCount += messagePayload.length;

        // get length
        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        byte[] messageLength = messageLengthBuffer.array();

        // write length, value, payload
        outputStream.write(messageLength);
        outputStream.write(messageType);
        outputStream.write(messagePayload);

        return outputStream.toByteArray();
    }

    // create piece message (value 7)
    public byte[] pieceMessage(int pieceIndex, byte[] pieceContent) throws IOException{

        // init output buffer & length
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;

        // set value to 7
        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 7;

        // payload consists of 4 byte index and the content byte array
        ByteBuffer pieceIndexBuffer = ByteBuffer.allocate(4);
        pieceIndexBuffer.putInt(pieceIndex);
        byte[] pieceIndexByte = pieceIndexBuffer.array();
        lengthCount += pieceIndexByte.length;

        // add length of content array
        lengthCount += pieceContent.length;

        // get length
        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        byte[] messageLength = messageLengthBuffer.array();

        // write length, value, index and piece content
        outputStream.write(messageLength);
        outputStream.write(messageType);
        outputStream.write(pieceIndexByte);
        outputStream.write(pieceContent);

        return outputStream.toByteArray();
    }

    public static void main(String arg[]) throws IOException {
        // do any necessary main functions
    }
    
}