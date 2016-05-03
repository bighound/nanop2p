package es.um.redes.P2P.PeerTracker.Client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import es.um.redes.P2P.PeerTracker.Message.Message;
import es.um.redes.P2P.util.FileInfo;

public class Reporter extends Thread {
	/**
	 * Path to local directory whose content is shared in network
	 */
	private String sharedFolderPath;
	/**
	 * Tracker hostname, for establishing connection
	 */
	private String trackerHostname;		
	
	
	private static final int PORT = 4450;
	/**
	 * Constructor: 
	 * @param sharedFolder Path to the shared folder of this peer, relative to $HOME
	 */
	public Reporter(String name, String sharedFolder, String tracker) {
		super(name);
		//Use getProperty("user.home") instead of System.getenv("HOME") for platform independent code
		if (new File(sharedFolder).isAbsolute()) {
			sharedFolderPath = sharedFolder;
		}
		else {
			sharedFolderPath = new String(System.getProperty("user.home")+"/"+sharedFolder); 
		}

		trackerHostname = tracker;
	}
	
	public Message sendMsg(byte requestOpcode, FileInfo[] file) throws IOException{
		// Abrimos el socket
		DatagramSocket socket = new DatagramSocket();
		
		// Creamos el buffer
		byte[] buf;
		
		// Preparamos mensaje
		Message message = Message.makeRequest(requestOpcode, PORT, file);
		buf=message.toByteArray();
		
		// Enviamos el mensaje
		InetSocketAddress addr = new InetSocketAddress(trackerHostname, PORT);	
		DatagramPacket packet = new DatagramPacket(buf, buf.length, addr);
		socket.send(packet);
		//System.out.println(message.toString() + "\n");
		
		
		// Recibimos la respuesta
		buf = new byte[Message.MAX_UDP_PACKET_LENGTH];
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		message = Message.parseResponse(buf);
		
		// Cerramos el socket
		socket.close();
		
		return message;
		
	}

	
	/*public void run() {
		System.out.println("Reporter starting conversation with tracker at "+trackerHostname);
		System.out.println("Shared folder path is "+sharedFolderPath);

		// TODO: Ver ejercicios del boletín de sockets UDP
		
		FileInfo[] localFile = FileInfo.loadFilesFromFolder(sharedFolderPath);
		
		try {
			// Apartado a
			System.out.println("-----Apartado a-----");
			Message messagea = sendMsg(Message.OP_ADD_SEED, localFile);
			// Devuelve un mensaje ADD_SEDD_ACK
			System.out.println(messagea.toString() + "\n");
			
			
			// Apartado b
			System.out.println("-----Apartado b-----");
			Message messageb = sendMsg(Message.OP_QUERY_FILES, localFile);
			// Devuelve un mensaje LIST_FILES
			System.out.println(messageb.toString() + "\n");
			
			// Apartado c
			System.out.println("-----Apartado c-----");
			FileInfo[] filec = new FileInfo[1];
			filec[0] = localFile[0];
			Message messagec = sendMsg(Message.OP_GET_SEEDS, filec);
			// Devuelve un mensaje SEED_LIST
			System.out.println(messagec.toString() + "\n");
			
			// Apartado d
			System.out.println("-----Apartado d-----");
			FileInfo[] filed = new FileInfo[1];
			filed[0] = localFile[0];
			Message messaged = sendMsg(Message.OP_REMOVE_SEED, filed);
			System.out.println(messaged.toString() + "\n");
			// Comprobamos que se ha borrado
			Message messagedq = sendMsg(Message.OP_QUERY_FILES, localFile);		
			System.out.println(messagedq.toString() + "\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		
		System.out.println("Reporter ending conversation with tracker");

	}*/
}
