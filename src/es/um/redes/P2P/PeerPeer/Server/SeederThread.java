package es.um.redes.P2P.PeerPeer.Server;
import java.io.BufferedReader;
//import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import es.um.redes.P2P.PeerPeer.Client.Downloader;
//import javax.xml.bind.DatatypeConverter;

//import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import es.um.redes.P2P.App.Peer;
//import es.um.redes.P2P.PeerPeer.Client.DownloaderThread;
//import es.um.redes.P2P.PeerPeer.Message.Message;
import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
//import es.um.redes.P2P.util.FileInfo;
//import javafx.scene.media.AudioClip;
//import jdk.management.resource.internal.inst.FileInputStreamRMHooks;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * Hilo que se ejecuta cada vez que se conecta un nuevo cliente.
 */
public class SeederThread extends Thread {
	private Socket socket = null;
	private String folderName;
	private String localname;
	private boolean all_chunks_received;

	public SeederThread(Socket socket, String folder) {
		super("SeederThread");
		this.socket = socket;
		this.folderName = folder;

	}

	/** Función de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			while(!all_chunks_received){
			// Recibo de datos
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String s = br.readLine();
			//System.out.println("Ha recibido(el seeder): " + s );
			
			
			String m;
			switch (MessageP.parseResponse(s)) {
			case 1: 
                //requestChunks(contenido);
				//System.out.println("Esta es una prueba para saber si lo coge bien el hash: "+MessageP.getHash()+"y el chunk: "+MessageP.getChunkNumber());
				
				//System.out.println(Peer.localfiles.length);
				for (int i = 0; i < Peer.localfiles.length; i++) {
					if(Peer.localfiles[i].fileHash.equals(MessageP.getHash())){
						localname=Peer.localfiles[i].fileName;
					}
				}
				
				//MessageP.setFilename("pepe");

				File file = new File(this.folderName + "\\"+localname);
				//System.out.println("Al final si lo coge: "+localname);
				int pos = MessageP.getChunkNumber()*Downloader.CHUNK_SIZE; // calculates the position in the file
				byte chunk[] = new byte[Downloader.CHUNK_SIZE];
				RandomAccessFile rfi = new RandomAccessFile(file,"r");
				rfi.seek(pos);
				rfi.read(chunk);
				rfi.close();
				
				m=MessageP.createMessageSend(chunk,MessageP.getChunkNumber());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				dos.writeUTF(m);
				System.out.println("Ha enviado el chunkNumber: "+MessageP.getChunkNumber());
				System.out.println("Lleva enviado : "+pos);
				
				
				//System.out.println("El seeder ha enviado un send_chunk: "+ m);
                break;
           
            case 3:  // all_chunks_received Si me envian un all chunks received entonces le env�o un mensaje de correcto:
            	String sonido = "Audios\\allchunksreceived.wav";
            	InputStream in = new FileInputStream(sonido);
            	AudioStream audio = new AudioStream(in);
            	AudioPlayer.player.start(audio);
            	all_chunks_received=true;
                break;
            case 4:   //File not found
            	//m = MessageP.createMessageNot("Este es un hash");
				//socket.getOutputStream().write(m.getBytes());
				//System.out.println("File no encontrado: "+ m);
                //fileNotFound(contenido);
                break;
            default:
                System.out.println("Mensaje con formato no correcto");
                break;
			}
			
			
			}
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}