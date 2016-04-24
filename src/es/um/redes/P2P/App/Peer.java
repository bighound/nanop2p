package es.um.redes.P2P.App;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import es.um.redes.P2P.PeerTracker.Client.Reporter;
import es.um.redes.P2P.PeerTracker.Message.Message;
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
			
			//Add seeds, query files, download, getseeds, removeseed, exit
			switch (arg[0]) {
			case "addseeds":
				System.out.println((client.sendMsg(Message.OP_ADD_SEED, localFile)).toString());
				break;
			case "queryfiles":
				Message mensaje = client.sendMsg(Message.OP_QUERY_FILES, localFile);
				System.out.println(mensaje.toString());			
				break;
			case "download":
				System.out.println("Descargando...");
				break;
			case "getseeds":
				if (arg[1] != null){
					System.out.println("Comando utilizado correctamente con el argumento" + arg[1]);
				}
				System.out.println((client.sendMsg(Message.OP_GET_SEEDS, localFile)).toString());
				break;
			case "removeseeds":
				System.out.println((client.sendMsg(Message.OP_REMOVE_SEED, localFile)).toString());
				break;	
			
			case "cliente":
				Socket socketCliente = new Socket("127.0.0.1",4450);
				//socketCliente.connect(null);
				break;
			case "servidor":
				ServerSocket socketServer = new ServerSocket(4450);
				Socket cliente = socketServer.accept();
				System.out.println("Esta prueba ha sido un exito rotundo en todos los paises de habla hispana ");
				/*Socket cliente = socketServidor.accept();
				cliente.setSoLinger(true,10);
				
				ObjectInputStream ois = new ObjectInputStream(cliente.getInputStream());
				Object mensaje = ois.readObject();*/
				
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
