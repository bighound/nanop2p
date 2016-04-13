package es.um.redes.P2P.App;


import es.um.redes.P2P.PeerTracker.Client.Reporter;

public class Peer {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Peer <tracker_hostname> <local_shared_folder>");
			return;
		}
		String trackerHostname = args[0];
		String peerSharedFolder = args[1];

		// Create client object
		Reporter client = new Reporter("PeerThread", peerSharedFolder, trackerHostname); 
		// Start client thread
		client.start();
	}
}
