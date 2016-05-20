package es.um.redes.P2P.PeerTracker.Server;

import java.io.*;
import java.net.*;

import es.um.redes.P2P.App.Tracker;
import es.um.redes.P2P.PeerTracker.Message.Message;
import es.um.redes.P2P.PeerTracker.Message.MessageControl;
import es.um.redes.P2P.PeerTracker.Message.MessageDataFileInfo;
import es.um.redes.P2P.PeerTracker.Message.MessageDataSeedInfo;
//import es.um.redes.P2P.PeerTracker.MessageP.ProtocolState;
import es.um.redes.P2P.util.FileInfo;


public class TrackerThread extends Thread {
	
	protected DatagramSocket socket = null;
	protected boolean running = true;

	public TrackerThread(String name) throws IOException {
		super(name);
		InetSocketAddress serverAddress = new InetSocketAddress(Tracker.TRACKER_PORT);
		socket = new DatagramSocket(serverAddress);

		running = true;
	}

	public void run() {
		byte[] buf = new byte[Message.MAX_UDP_PACKET_LENGTH];

		System.out.println("Tracker starting...");

		while(running) {
			try {

				// 1) Receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				// 2) Figure out client 
				InetSocketAddress clientId = (InetSocketAddress) packet.getSocketAddress();

				// 3) Parse request and get type  
				Message request = Message.parseRequest(buf);
				
				if (request == null) {
					// Request failed to be parsed (e.g. due to CRC failure): Discard
					// Client will retransmit when timeout expires
					continue;
				}

				System.out.println("Tracker received request "+request+" from "+clientId);
				processRequestFromPeer(request, clientId.getAddress());				
				
				// 4) Stateless tracker: always accepts requests 

				// 5) Make response from this request
				Message response = makeResponse(request);				
				assert(response != null);

				// 6) Create datagram packet from response 
				byte [] responseBuf = response.toByteArray();
				// 7) Send response back to client at address
				packet = new DatagramPacket(responseBuf, responseBuf.length, clientId);
				socket.send(packet);
				System.out.println("Tracker sent response: "+response);

			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			}
		}
		socket.close();
	}
	
	/**
	 * Method that returns the corresponding tracker response message to a given request.
	 * @param request The request received by the tracker
	 * @param clientId The client peer that sent this request
	 * @return The response message that should be sent to the client
	 */
	public Message makeResponse(Message request) {
		byte response_opcode = request.getResponseOpcode();
		switch (response_opcode) {
			case Message.OP_REMOVE_SEED_ACK:
			case Message.OP_ADD_SEED_ACK:
				{
					return new MessageControl(response_opcode);
				}
			case Message.OP_FILE_LIST:
				{
					assert(request instanceof MessageControl); // Query files
					FileInfo[] fileList = TrackerDatabase.db.getFileInfoList();
					return new MessageDataFileInfo(response_opcode, fileList);
				}
			case Message.OP_SEED_LIST:
				{
					assert(request instanceof MessageDataSeedInfo); // Get seeds
					String fileHash = ((MessageDataSeedInfo)request).getFileHash();
					InetSocketAddress[] seedList = TrackerDatabase.db.getSeeds(fileHash);
					return new MessageDataSeedInfo(response_opcode, seedList, fileHash);
				}
			default:
		}
		assert(false); // Should never reach this point
		return null;		
	}

	/**
	 * Process a control request received by the tracker, from the given client. 
	 * Updates tracker database if necessary (currently, only upon disconnect.
	 */
	public void processRequestFromPeer(Message request, InetAddress clientAddr) {
		switch(request.getOpCode()) {
		case Message.OP_ADD_SEED:
		case Message.OP_REMOVE_SEED:
			int seederPort = ((MessageDataFileInfo)request).getPort();
			InetSocketAddress seedId = new InetSocketAddress(clientAddr, seederPort);
			FileInfo[] fileList = ((MessageDataFileInfo)request).getFileList();
			if (request.getOpCode() == Message.OP_ADD_SEED) {
				/* Tracker database uses socket address (IP+port where each peer listens
				 * for connections from other peers) as seed identifiers
				 */
				TrackerDatabase.db.addSeedToFileList(fileList, seedId);
			}
			else if (fileList.length > 0) {
				/* Update database, removing this seed from the given files' seed lists */
				TrackerDatabase.db.removeSeedFromFileList(fileList, seedId);
			}
			else {
				/* Update database, removing this seed from all seed lists, removing 
				 * record for the file if seed list empty */
				TrackerDatabase.db.disconnectPeer(seedId);
			}
			break;
		case Message.OP_QUERY_FILES:
		case Message.OP_GET_SEEDS:
			/* These requests do not require any update in database 
			 * */
		default:
		}
	}	
}
