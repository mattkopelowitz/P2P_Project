import java.io.*;
import java.util.*;
import java.net.Socket;
import java.nio.*;

// handles incoming messages with runnable
public class MessageHandler implements Runnable{
    public void run(){

        // implement switch case for message type

        //case 0: log choked

        //case 1: log unchoked, if peer has a necessary file, send request

        //case 2: log interested

        //case 3: log not interested

        //case 4: log peer have, send interested/ not interested

        //case 5: save peer's bitfield, send interested/ not interested

        //case 6: log request, send piece if unchoked

        //case 7: take in piece, send have to peers
    }
}
