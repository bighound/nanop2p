package es.um.redes.P2P.PeerPeer.Client;

import es.um.redes.P2P.PeerPeer.Message.Message;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DownloaderThread extends Thread {
	private Socket socket = null;
	private Downloader downloader = null;
	private String fileHash;

	public DownloaderThread(Downloader downloader, Socket socket, String fileHash) {
		super("DowloaderThread");
		this.socket = socket;
		this.downloader = downloader;
		this.fileHash = fileHash;
	}

	/** Función de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// En un Socket, para enviar hay que usar su OutputStream
			OutputStream os = socket.getOutputStream();
			System.out.println("Ha enviado(el downloader): " + fileHash);
			//String msg = Message.createMessageRequest(fileHash, 1);
			/*
			 * Enviamos el mensaje de Request chunks
			 */
			String msg = Message.createMessageRequest(fileHash,1);
			socket.getOutputStream().write(msg.getBytes());

			/*
			 * Se pone a la escucha de posibles mensajes.
			 */
			InputStream is = socket.getInputStream();
			byte buffer[] = new byte[150];
			is.read(buffer);
			String s = new String(buffer, 0, buffer.length);
			System.out.println("Ha recibido(soy el downloader): " + s);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
