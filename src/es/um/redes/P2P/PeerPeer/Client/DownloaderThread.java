package es.um.redes.P2P.PeerPeer.Client;

//import es.um.redes.P2P.PeerPeer.Message.Message;
import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
//import es.um.redes.P2P.PeerPeer.Server.Seeder;
//import es.um.redes.P2P.PeerPeer.Server.SeederThread;
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

//import com.sun.org.apache.bcel.internal.generic.AALOAD;

public class DownloaderThread extends Thread {


	private Socket socket = null;
	private Downloader downloader = null;
	private FileInfo file;
	private String folderName;
	private int nChunks;

	public DownloaderThread(Downloader downloader, Socket socket, FileInfo file, String folder,int nChunks) {
		super("DowloaderThread");
		this.socket = socket;
		this.downloader = downloader;
		this.file = file;
		this.folderName = folder;
		this.nChunks=nChunks;
	}
	int i = 0;
	
	public Downloader getDownloader() {
		return downloader;
	}

	/** Funci√≥n de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// En un Socket, para enviar hay que usar su OutputStream
			for (i = 0; i < nChunks; i++) {
				
			
			// Con esta parte del cÛdigo, enviamos el socket, que contendr· el mensaje de request_chunk
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());					
			//System.out.println("Ha enviado(el downloader): " + file.fileHash);			
			String msg = MessageP.createMessageRequest(file.fileHash,i);
			//System.out.println("Ha enviado: "+msg);
			os.writeUTF(msg);


			
			
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));			
			String s = br.readLine();			
			//System.out.println("Esto es lo que vale s : "+s);
			switch (MessageP.parseResponse(s)){
			case 2: 
				//byte chunk[] = new byte[CHUNK_SIZE];
				int pos = MessageP.getChunkNumber()*Downloader.CHUNK_SIZE;
				File f = new File(folderName + "\\" + file.fileName);
				if (!f.exists()) {
					f.createNewFile();
					}		
				RandomAccessFile rfo = new RandomAccessFile(f, "rw");
				rfo.seek(pos);
				rfo.write(MessageP.getChunk(), 0, Downloader.CHUNK_SIZE);
				rfo.close();
				}
				
			}
			System.out.println("-----------------All chunks have been sent-----------------");
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());
			os.writeUTF(MessageP.createMessageAll());
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
