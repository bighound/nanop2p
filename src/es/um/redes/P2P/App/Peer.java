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
		String hash=null;
		String hashQF=null;
		
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
				// Si hacemos un queryfiles sin haber hecho nada salta una excepción
			case "queryfiles":                                
				mensaje = client.sendMsg(Message.OP_QUERY_FILES, localFile);
				System.out.println(mensaje.toString());
				MessageDataFileInfo mdf;
				mdf = (MessageDataFileInfo) mensaje;
				FileInfo[] file;
				file = mdf.getFileList();				
				break;
			case "download":
				
				//cosa nueva que añado
				Socket socketCliente = new Socket("127.0.0.1",4450);
				String salida= arg[1]+" "+arg[2];
				socketCliente.getOutputStream().write(salida.getBytes());
				//System.out.println("Lo que recibe es: ",s);    
				
				
				
				//Lo que está entre estas barras es mio by bighound
				
				InputStream isD = socketCliente.getInputStream();
				byte buf[] = new byte[56];
				isD.read(buf);
				String sD = new String(buf, 0, buf.length);
				System.out.println("Recibida contestacion: " + sD);
				break;
			case "getseeds":
				mensaje =client.sendMsg(Message.OP_GET_SEEDS, localFile);
				MessageDataSeedInfo mds;
				mds = (MessageDataSeedInfo) mensaje;
				InetSocketAddress []direcciones;
				direcciones = mds.getSeedList();
				//System.out.println(direcciones[0].getHostName());
				System.out.println("Descargando el fichero : " + hashQF);
				String ip=direcciones[0].getHostName();
				int port=direcciones[0].getPort();
				/*
				mensaje =client.sendMsg(Message.OP_GET_SEEDS, localFile);
				System.out.println(mensaje.toString());
				MessageDataSeedInfo mds;
				mds = (MessageDataSeedInfo) mensaje;
				InetSocketAddress []direcciones;
				direcciones = mds.getSeedList();
				System.out.println(direcciones[0].getHostName());
				*/
				
				
				//-----------------cosa mia--
				break;
			case "removeseeds":
				System.out.println((client.sendMsg(Message.OP_REMOVE_SEED, localFile)).toString());
				break;	
			case "servidor":
				ServerSocket socketServer = new ServerSocket(4450);
				Socket cliente = socketServer.accept();
				// Cuando este acepte un getseeds, tiene que coger el hash que le piden y contestar con el.    Hecho
				byte[] buffer = new byte[56];
				InputStream is = cliente.getInputStream();
				is.read(buffer);
				String s = new String(buffer, 0, buffer.length);
				System.out.println("Lo que recibe es: " + s);
				
				
				
				cliente.getOutputStream().write(s.getBytes());
				
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
