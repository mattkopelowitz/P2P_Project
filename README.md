# P2P_Project

Group Number: 26

Team Members:
- Kendall Stansfield-Phillips (kstansfieldphill@ufl.edu) 
- Matthew Kopelowitz (m.kopelowitz@ufl.edu)
- Max Fennessy (maxfennessy@ufl.edu)

Contribution of Each Team Members

- Matthew worked on peerProcess, Peer, and LogWriter
- Kendall worked on Message and MessageHandler
- Max worked on Server and Client

YouTube video link: https://youtu.be/ptZTXl4aeLA


What you were able to achieve and what you were not:
- We were able to code all of the functionality, but were not able to finish debugging it to allow for the peers to share files properly. In testing, our project can use Common.cfg and PeerInfo.cfg to set varibles, start all of the peers and connect them over TCP. We could not get the peers to exchange handshake messages, so they are not able to send or recieve files currently, but the code should support all message functionality if debugged.

How To Run the Project:

1. Download the project zip
2. Unzip to a folder
3. Open a command line in the P2P_Project/src directory
4. Run: javac peerProcess.java
5. Run: java peerProcess 1001
6. Run (java peerProcess [peerNumber]) in all terminals in order of the peerNumbers listed in peerInfo.cfg

How to Run the Project on CISE machine:

1. SSH into cise machine in six different terminals (run: ssh username@thunder.cise.ufl.edu)
2. clone the git repository
3. cd into the P2P_Project/src directory
4. Run (javac peerProcess.java) in the first terminal
5. Run (java peerProcess [peerNumber]) in all terminals in order of the peerNumbers listed in peerInfo.cfg
