package es.um.redes.P2P.PeerPeer.Client;

import es.um.redes.P2P.PeerPeer.Message.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DownloaderThread extends Thread {
	private Socket socket = null;
	private Downloader downloader = null;
	private String fileHash;
	private long fileS;
	private String folderName;

	public DownloaderThread(Downloader downloader, Socket socket, String fileHash, long fileS, String folder) {
		super("DowloaderThread");
		this.socket = socket;
		this.downloader = downloader;
		this.fileHash = fileHash;
		this.fileS = fileS;
		this.folderName = folder;
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
			
			byte buffer[] = new byte[(int) fileS];
			socket.getInputStream().read(buffer);
			//String s = new String(buffer, 0, buffer.length);
			//System.out.println("Ha recibido(soy el downloader): " + s);
			
			File f = new File(folderName + "\\a.txt");
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(buffer);
			fos.close();
			
			/*ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			Object mensaje = ois.readObject();
			ois.read((byte[]) mensaje);*/
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
