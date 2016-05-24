package es.um.redes.P2P.PeerPeer.Client;

import es.um.redes.P2P.PeerPeer.MessageP.MessageCode;
import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
import es.um.redes.P2P.util.FileInfo;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.Semaphore;


class DownloaderThread extends Thread {


	private Socket socket = null;
	private Downloader downloader = null;
	private FileInfo file;
	private String folderName;
	private boolean fin;
	private Semaphore mutex;
	private int threadNum;

	DownloaderThread(Downloader downloader, Socket socket, FileInfo file, String folder,Semaphore mut, int threadNumber) {
		super("DowloaderThread");
		this.socket = socket;
		this.downloader = downloader;
		this.file = file;
		this.folderName = folder;
		this.fin = false;
		this.mutex = mut;
		this.threadNum = threadNumber;
	}

	private int getNextChunk() throws InterruptedException {
		int num = -1;
		mutex.acquire();
		for (int j = 0; j < downloader.chunkSeen.length; j++) {
			if(!downloader.chunkSeen[j]){
				num = j;
				downloader.chunkSeen[j] = true;
				downloader.chunkPerThread[this.threadNum]++;
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
			boolean notFound = false;
			while (!fin) {
				int chunkNumber = getNextChunk();
				if(chunkNumber == -1) {
					break;
				}

				MessageP msg = new MessageP(MessageCode.REQUEST_CHUNK, file.fileHash, chunkNumber, null);
				os.writeUTF(msg.toString());
				InputStream is = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s = br.readLine();
				MessageP received = new MessageP();
				switch (received.parseResponse(s)){
					case SEND_CHUNK:
						int numero = received.getChunkNumber();
						byte [] chunkData = received.getChunk();

						int pos =numero*Downloader.CHUNK_SIZE;
						File f = new File(folderName + "\\" + file.fileName);
						if (!f.exists()) {
							f.createNewFile();
						}
						RandomAccessFile rfo = new RandomAccessFile(f, "rw");
						rfo.seek(pos);
						rfo.write(chunkData, 0, chunkData.length);
						rfo.close();
						break;
					case FILE_NOT_FOUND:
						fin=true;
						notFound = true;
						System.out.println("Fichero no encontrado. FILE_NOT_FOUND recibido en DownloaderThread");
						break;
					case INVALID_CODE:
						fin=true;
						notFound=true;
						break;
				}

			}
			if(!notFound){
				DataOutputStream osFinal = new DataOutputStream(socket.getOutputStream());
				MessageP all = new MessageP(MessageCode.ALL_CHUNKS_RECEIVED, null, -1, null);
				osFinal.writeUTF(all.toString());
				os.close();
				osFinal.close();
			}else {
				os.close();
			}

		} catch (Exception e) {
			System.out.println("Error en la descarga o en la conexion con el seeder");
		}
	}
}
