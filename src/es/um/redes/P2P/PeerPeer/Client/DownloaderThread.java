package es.um.redes.P2P.PeerPeer.Client;

//import es.um.redes.P2P.PeerPeer.Message.Message;
import es.um.redes.P2P.PeerPeer.MessageP.MessageCode;
import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
//import es.um.redes.P2P.PeerPeer.Server.Seeder;
//import es.um.redes.P2P.PeerPeer.Server.SeederThread;
import es.um.redes.P2P.PeerTracker.Message.Message;
import es.um.redes.P2P.util.FileInfo;
import es.um.redes.P2P.PeerPeer.Client.Downloader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
//import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.Semaphore;

//import com.sun.org.apache.bcel.internal.generic.AALOAD;

public class DownloaderThread extends Thread {


	private Socket socket = null;
	private Downloader downloader = null;
	private FileInfo file;
	private String folderName;
	private boolean fin;
	private Semaphore mutex;

	DownloaderThread(Downloader downloader, Socket socket, FileInfo file, String folder,Semaphore mut) {
		super("DowloaderThread");
		this.socket = socket;
		this.downloader = downloader;
		this.file = file;
		this.folderName = folder;
		this.fin = false;
		this.mutex = mut;
	}

	private int getNextChunk() throws InterruptedException {
		int num = -1;
		mutex.acquire();
		for (int j = 0; j < downloader.chunkSeen.length; j++) {
			if(!downloader.chunkSeen[j]){
				num = j;
				downloader.chunkSeen[j] = true;
				mutex.release();
				return num;
			}
		}
		mutex.release();
		return num;
	}


	/** Función de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// En un Socket, para enviar hay que usar su OutputStream
			// Con esta parte del c�digo, enviamos el socket, que contendr� el mensaje de request_chunk
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());

			while (!fin) {
				int chunkNumber = getNextChunk();
				if(chunkNumber == -1) break;

				MessageP msg = new MessageP(MessageCode.REQUEST_CHUNK, file.fileHash, chunkNumber, null);
				os.writeUTF(msg.toString());
				InputStream is = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s = br.readLine();
				MessageP received = new MessageP();
				switch (received.parseResponse(s)){
					case SEND_CHUNK:
						int numero = received.getChunkNumber();
						int pos =numero*Downloader.CHUNK_SIZE;
						File f = new File(folderName + "\\" + file.fileName);
						if (!f.exists()) {
							f.createNewFile();
						}
						RandomAccessFile rfo = new RandomAccessFile(f, "rw");
						rfo.seek(pos);
						rfo.write(received.getChunk(), 0, Downloader.CHUNK_SIZE);
						rfo.close();
				}

			}
			System.out.println("-----------------Descarga completada-----------------");
			DataOutputStream osFinal = new DataOutputStream(socket.getOutputStream());
			MessageP all = new MessageP(MessageCode.ALL_CHUNKS_RECEIVED, null, -1, null);
			osFinal.writeUTF(all.toString());
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
