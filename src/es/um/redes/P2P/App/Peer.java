package es.um.redes.P2P.App;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;



import es.um.redes.P2P.PeerTracker.Client.Reporter;
import es.um.redes.P2P.PeerTracker.Message.Message;
import es.um.redes.P2P.PeerTracker.Message.MessageDataFileInfo;
import es.um.redes.P2P.PeerTracker.Message.MessageDataSeedInfo;
import es.um.redes.P2P.util.FileInfo;

public class Peer {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: java Peer <tracker_hostname> <local_shared_folder>");
			return;
		}
		String trackerHostname = args[0];
		String peerSharedFolder = args[1];
		Boolean continua = true;
		
		// Create client object
		Reporter client = new Reporter("PeerThread", peerSharedFolder, trackerHostname); 
		// Start client thread
		client.start();
		
		FileInfo[] localFile = FileInfo.loadFilesFromFolder(peerSharedFolder);
		
		System.out.println("Comandos: addseeds, queryfiles, download, getseeds, removeseed, exit");
		while (continua) {
			Scanner scan = new Scanner(System.in);
			String entrada = scan.nextLine();
			String [] arg = entrada.split("\\s+");
			Message mensaje;
			
			//Add seeds, query files, download, getseeds, removeseed, exit
			switch (arg[0]) {
			case "addseeds":
				System.out.println((client.sendMsg(Message.OP_ADD_SEED, localFile)).toString());
				break;
			case "queryfiles":
				mensaje = client.sendMsg(Message.OP_QUERY_FILES, localFile);
				System.out.println(mensaje.toString());
				MessageDataFileInfo mdf;
				mdf = (MessageDataFileInfo) mensaje;
				FileInfo[] file;
				file = mdf.getFileList();
				System.out.println(file[0].toString());
				System.out.println(file[0].fileHash);
				
				break;
			case "download":
				System.out.println("Descargando...");
				break;
			case "getseeds":
				/*if (arg[1] != null){
					System.out.println("Comando utilizado correctamente con el argumento" + arg[1]);
				}*/
				mensaje =client.sendMsg(Message.OP_GET_SEEDS, localFile);
				System.out.println(mensaje.toString());
				MessageDataSeedInfo mds;
				mds = (MessageDataSeedInfo) mensaje;
				InetSocketAddress []direcciones;
				direcciones = mds.getSeedList();
				System.out.println(direcciones[0].getHostName());
				
				break;
			case "removeseeds":
				System.out.println((client.sendMsg(Message.OP_REMOVE_SEED, localFile)).toString());
				break;	
			
			case "cliente":
				Socket socketCliente = new Socket("127.0.0.1",4450);
				String salida= "Jorgec10 headshot BigHound";
				socketCliente.getOutputStream().write(salida.getBytes());
				//socketCliente.connect(null);
				break;
			case "servidor":
				ServerSocket socketServer = new ServerSocket(4450);
				Socket cliente = socketServer.accept();
				// Cuando este acepte un getseeds, tiene que coger el hash que le piden y contestar con el.
				byte[] buffer = new byte[50];
				InputStream is = cliente.getInputStream();
				is.read(buffer);
				String s = new String(buffer, 0, buffer.length);
				System.out.println(s);			
				break;	
			case "exit":
				continua=false;
				System.out.println("Fin del proceso del peer");
				break;
			default:
				System.out.println("Comando incorrecto");
				System.out.println("Comandos: addseeds, queryfiles, download, getseeds, removeseed, exit");
				break;
			}
		}
		
		
		
		
		
	}
}
