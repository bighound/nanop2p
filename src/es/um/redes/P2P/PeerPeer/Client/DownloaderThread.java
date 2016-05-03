package es.um.redes.P2P.PeerPeer.Client;

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
			socket.getOutputStream().write(fileHash.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
