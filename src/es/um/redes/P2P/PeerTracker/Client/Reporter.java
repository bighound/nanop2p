package es.um.redes.P2P.PeerTracker.Client;

import java.io.IOException;
import java.net.*;
//import java.net.SocketException;

import es.um.redes.P2P.PeerTracker.Message.Message;
import es.um.redes.P2P.util.FileInfo;

public class Reporter extends Thread {
	/**
	 * Tracker hostname, for establishing connection
	 */
	private String trackerHostname;
	private int seedPort;
	
	
	private static final int PORT = 4450;
	/**
	 * Constructor: 
	 * @param sharedFolder Path to the shared folder of this peer, relative to $HOME
	 */
	
	public Reporter(String name, String sharedFolder, String tracker, int port) {
		super(name);
		//Use getProperty("user.home") instead of System.getenv("HOME") for platform independent code
		/*
		  Path to local directory whose content is shared in network
		*/
		trackerHostname = tracker;
		seedPort = port;
	}

	public Message sendMsg(byte requestOpcode, FileInfo[] file) throws IOException{
		// Abrimos el socket
		DatagramSocket socket = new DatagramSocket();
		
		// Creamos el buffer
		byte[] buf;
		
		// Preparamos mensaje
		Message message = Message.makeRequest(requestOpcode, seedPort, file);
		InetSocketAddress addr;
		DatagramPacket packet;
		if (message != null) {
			buf=message.toByteArray();
			// Enviamos el mensaje
			addr = new InetSocketAddress(trackerHostname, PORT);
			packet = new DatagramPacket(buf, buf.length, addr);
			socket.send(packet);
		}
		// Recibimos la respuesta
		buf = new byte[Message.MAX_UDP_PACKET_LENGTH];
		packet = new DatagramPacket(buf, buf.length);

		try{
			socket.setSoTimeout(1000);
			socket.receive(packet);
		} catch(SocketTimeoutException e1){
			System.out.println("Timeout excedido en la recepcion de un mensaje del tracker");
		}
		message = Message.parseResponse(buf);
		
		// Cerramos el socket
		socket.close();
		
		return message;
		
	}
}
