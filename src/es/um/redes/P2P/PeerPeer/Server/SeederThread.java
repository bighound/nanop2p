package es.um.redes.P2P.PeerPeer.Server;
import java.io.*;
import java.net.Socket;
import es.um.redes.P2P.PeerPeer.Client.Downloader;
import es.um.redes.P2P.App.Peer;
import es.um.redes.P2P.PeerPeer.MessageP.MessageCode;
import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * Hilo que se ejecuta cada vez que se conecta un nuevo cliente.
 */
class SeederThread extends Thread {
	private Socket socket = null;
	private String folderName;
	private String localname;
	private boolean all_chunks_received;

	SeederThread(Socket socket, String folder) {
		super("SeederThread");
		this.socket = socket;
		this.folderName = folder;

	}

	/** Función de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			int chunkNumber;
			while(!all_chunks_received){
				// Recibo de datos
				InputStream is = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String s = br.readLine();
				MessageP received = new MessageP();
				switch (received.parseResponse(s)) {
					case REQUEST_CHUNK:
						chunkNumber = received.getChunkNumber();
						int fileSize = 0;
						for (int i = 0; i < Peer.localfiles.length; i++) {
							if(Peer.localfiles[i].fileHash.equals(received.getHash())){
								localname=Peer.localfiles[i].fileName;
								fileSize = (int) Peer.localfiles[i].fileSize;
							}
						}
						int numChunks = fileSize/Downloader.CHUNK_SIZE;
						int restante = fileSize % Downloader.CHUNK_SIZE;
						int byteSize;
						if(chunkNumber == numChunks) byteSize = restante;
						else byteSize = Downloader.CHUNK_SIZE;


						File file = new File(this.folderName + "\\"+localname);
						int pos = chunkNumber*Downloader.CHUNK_SIZE; // calculates the position in the file
						byte chunk[] = new byte[byteSize];
						try {
							RandomAccessFile rfi = new RandomAccessFile(file,"r");
							rfi.seek(pos);
							rfi.read(chunk);
							rfi.close();

							MessageP enviar = new MessageP(MessageCode.SEND_CHUNK, null, chunkNumber, chunk);
							DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
							dos.writeUTF(enviar.toString());

							//System.out.println("Lleva enviado : " + (pos+Downloader.CHUNK_SIZE));
						} catch (FileNotFoundException f){
							System.out.println("Fichero no encontrado. Error en SeederThread.");
						}

						break;

					case ALL_CHUNKS_RECEIVED:  // all_chunks_received Si me envian un all chunks received entonces le env�o un mensaje de correcto:
						String sonido = "Audios\\allchunksreceived.wav";
						InputStream in = new FileInputStream(sonido);
						AudioStream audio = new AudioStream(in);
						AudioPlayer.player.start(audio);
						all_chunks_received=true;
						socket.close();
						break;

					case FILE_NOT_FOUND:   //File not found
						MessageP notFound = new MessageP(MessageCode.FILE_NOT_FOUND, null, -1, null);
						DataOutputStream tres = new DataOutputStream(socket.getOutputStream());
						tres.writeUTF(notFound.toString());
						break;
					case INVALID_CODE:
						all_chunks_received=true;
						socket.close();
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