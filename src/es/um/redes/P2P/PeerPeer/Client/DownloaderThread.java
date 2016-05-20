package es.um.redes.P2P.PeerPeer.Client;

import es.um.redes.P2P.util.FileInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class DownloaderThread extends Thread {

	private final int CHUNK_SIZE = 1024;


	private Socket socket = null;
	private Downloader downloader = null;
	private FileInfo file;
	private String folderName;

	public DownloaderThread(Downloader downloader, Socket socket, FileInfo file, String folder, int chunkNumber) {
		super("DowloaderThread");
		this.socket = socket;
		this.downloader = downloader;
		this.file = file;
		this.folderName = folder;
	}

	/** Función de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// En un Socket, para enviar hay que usar su OutputStream
			OutputStream os = socket.getOutputStream();
			System.out.println("Ha enviado(el downloader): " + file.fileHash);

			String msg = MessagePRequestChunk.createRequest(file.fileHash,1);
			
			//os.write(msg.getBytes());










			byte buffer[] = new byte[(int) file.fileSize];
			socket.getInputStream().read(buffer);

			File f = new File(folderName + "\\" + file.fileName);
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(buffer);
			fos.close();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
