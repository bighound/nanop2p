package es.um.redes.P2P.App;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


import es.um.redes.P2P.PeerPeer.Client.Downloader;
import es.um.redes.P2P.PeerPeer.Server.Seeder;
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

        FileInfo[] fileToSend = new FileInfo[1];

		Seeder seed = new Seeder();
		seed.init();
		
		// Create client object
		Reporter client = new Reporter("PeerThread", peerSharedFolder, trackerHostname);

		// Start client thread
		client.start();
		
		FileInfo[] localFile = FileInfo.loadFilesFromFolder(peerSharedFolder);
		
		System.out.println("Comandos: query, download, exit");
		while (continua) {
			Scanner scan = new Scanner(System.in);
			String entrada = scan.nextLine();
			String [] arg = entrada.split("\\s+");
			Message mensaje;
            MessageDataFileInfo mdf;
            MessageDataSeedInfo mds;


            //query, download, exit
			switch (arg[0]) {
			case "query":
				System.out.println((client.sendMsg(Message.OP_ADD_SEED, localFile)).toString());
				mensaje = client.sendMsg(Message.OP_QUERY_FILES, localFile);
				System.out.println(mensaje.toString());
                break;

			case "download":
                // Enviamos el query files para obtener la lista de ficheros del tracker
                mensaje = client.sendMsg(Message.OP_QUERY_FILES, localFile);
                mdf = (MessageDataFileInfo) mensaje;
                FileInfo[] file;
                file = mdf.getFileList();

                // Comprobamos el uso correcto del comando
                if (arg.length == 2){
                    hash = arg[1];
                } else {
                    System.out.println("Uso del comando: download <hash>");
                    break;
                }

                // Comprobamos que el fichero con ese hash está en el tracker.
                int posicion = -1;
                for (int i = 0; i < file.length; i++) {
                    if (file[i].fileHash.equals(hash)) posicion = i;
                }
                if (posicion == -1){
                    System.out.println("Hash no encontrado");
                    break;
                }

                // Enviamos el getseeds del hash deseado y obtenemos su IP y puerto
                fileToSend[0] = file[posicion];
				mensaje = client.sendMsg(Message.OP_GET_SEEDS, fileToSend);
				mds = (MessageDataSeedInfo) mensaje;
				InetSocketAddress [] dirs;
				dirs = mds.getSeedList();
                String ip = dirs[0].getAddress().toString().substring(1);
                int puerto = dirs[0].getPort();

				// Crear un objeto downloader
				Downloader down = new Downloader();
				down.download(ip, puerto, hash);
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
