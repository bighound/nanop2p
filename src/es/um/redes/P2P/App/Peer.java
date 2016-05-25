package es.um.redes.P2P.App;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import es.um.redes.P2P.PeerPeer.Client.Downloader;
import es.um.redes.P2P.PeerPeer.Server.Seeder;
import es.um.redes.P2P.PeerTracker.Client.Reporter;
import es.um.redes.P2P.PeerTracker.Message.Message;
import es.um.redes.P2P.PeerTracker.Message.MessageDataFileInfo;
import es.um.redes.P2P.PeerTracker.Message.MessageDataSeedInfo;
import es.um.redes.P2P.util.FileInfo;
import static java.lang.System.exit;

public class Peer {
	public static FileInfo localfiles[];

	private static String alinearDerecha(String cadena, int espacios) {
		return String.format("%1$-" + espacios + "s", cadena);
	}


	private static void imprimeQuery(FileInfo[] peerF, FileInfo[] trackerF){
		LinkedList<FileInfo> queryList = new LinkedList<>();
		for (FileInfo trackerFile: trackerF) {
			boolean flag = true;
			for (FileInfo peerFile: peerF) {
				if (trackerFile.fileHash.equals(peerFile.fileHash)){
					flag=false;
				}
			}
			if(flag){
				queryList.add(trackerFile);
			}
		}
		if(queryList.isEmpty()) System.out.println("No hay archivos para descargar en el tracker");
		else {
			System.out.println("Listado de archivos: ");
			System.out.println("\t" + alinearDerecha("Name", 25) +alinearDerecha("Size", 15) + alinearDerecha("Hash", 1));
			System.out.println("\t" + alinearDerecha("----", 25) +alinearDerecha("----", 15) + alinearDerecha("----", 1));
		}
		for (FileInfo file: queryList) {
			System.out.println("\t" + alinearDerecha(file.fileName, 25) + alinearDerecha((file.fileSize+""), 15) + alinearDerecha(file.fileHash, 1));
		}
	}


	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: java Peer <tracker_hostname> <local_shared_folder>");
			return;
		}
		String trackerHostname = args[0];
		String peerSharedFolder = args[1];
		Boolean continua = true;
		String hash;
		

        FileInfo[] fileToSend = new FileInfo[1];
        FileInfo[] localFile = FileInfo.loadFilesFromFolder(peerSharedFolder);
  
		Seeder seed = new Seeder(peerSharedFolder);
		seed.init();
		
		// Create client object
		Reporter client = new Reporter("PeerThread", peerSharedFolder, trackerHostname, seed.getPort());

		// Start client thread
		client.start();
		
		Downloader down = null;
				
		Scanner scan=null;
		System.out.println("Comandos: query, download <hash>, quit");
		while (continua) {
			scan = new Scanner(System.in);
			String entrada = "";
			try{
				entrada = scan.nextLine();
			}catch (NoSuchElementException e){
				
			}
			
			String [] arg = entrada.split("\\s+");
			Message mensaje;
            MessageDataFileInfo mdf;
            MessageDataSeedInfo mds;

			switch (arg[0]) {
			case "query":
				try{
					client.sendMsg(Message.OP_ADD_SEED, localFile);
					localFile  = FileInfo.loadFilesFromFolder(peerSharedFolder);
					mensaje = client.sendMsg(Message.OP_QUERY_FILES, localFile);
					mdf = (MessageDataFileInfo) mensaje;
					FileInfo[] filesTracker = mdf.getFileList();
					Peer.localfiles=filesTracker;
					FileInfo[] filesPeer = FileInfo.loadFilesFromFolder(peerSharedFolder);
					imprimeQuery(filesPeer, filesTracker);
				} catch (NullPointerException e){
					System.out.println("Fallo al intentar conectarse con el tracker");
					exit(1);
				}
				break;

			case "download":
                // Enviamos el query files para obtener la lista de ficheros del tracker
                FileInfo[] fileList = new FileInfo[0];
                try{
                    mensaje = client.sendMsg(Message.OP_QUERY_FILES, localFile);
                    mdf = (MessageDataFileInfo) mensaje;
                    fileList = mdf.getFileList();
                } catch (NullPointerException e){
                    System.out.println("Fallo al intentar conectarse con el tracker");
                    exit(1);
                }

                // Comprobamos el uso correcto del comando
                if (arg.length == 2){
                    hash = arg[1];
                } else {
                    System.out.println("Uso del comando: download <hash>");
                    break;
                }
                // Comprobamos que el fichero con ese hash está en el tracker.
                int posicion = -1;
                int ambiguo = 0;
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].fileHash.contains(hash)) {
                    	posicion = i; 
                    	ambiguo ++;
					 	if (ambiguo==2) posicion =-2;
                    }
                }
                if (posicion == -1){
                    System.out.println("Hash no encontrado");
                    break;
                }else if (posicion == -2){
                	System.out.println("Cadena ambigua");
                	break;
                }
                // Enviamos el getseeds del hash deseado y obtenemos su IP y puerto
                fileToSend[0] = fileList[posicion];

                InetSocketAddress [] dirs = new InetSocketAddress[0];
                try{
                    mensaje = client.sendMsg(Message.OP_GET_SEEDS, fileToSend);
                    mds = (MessageDataSeedInfo) mensaje;
                    dirs = mds.getSeedList();
                }catch (NullPointerException e){
                    System.out.println("Fallo al intentar conectarse con el tracker");
                    exit(1);
                }

				// Crear un objeto downloader
				down = new Downloader();
				down.download(dirs, fileToSend[0],peerSharedFolder);
                localFile = FileInfo.loadFilesFromFolder(peerSharedFolder);
                try{
                    client.sendMsg(Message.OP_ADD_SEED, localFile);
                } catch (NullPointerException e){
                    System.out.println("Fallo al intentar conectarse con el tracker");
                    exit(1);
                }
				break;				
            case "quit":
				continua=false;
				localFile = FileInfo.loadFilesFromFolder(peerSharedFolder);
				try{
                    client.sendMsg(Message.OP_REMOVE_SEED, localFile);
                } catch (NullPointerException e){
                    System.out.println("Fallo al intentar conectarse con el tracker");
                    exit(1);
                }
				System.out.println("Fin del proceso del peer");
				exit(0);
				break;
			default:
				System.out.println("Comando incorrecto");
				System.out.println("Comandos: query, download <hash>, quit");
				break;
			}
		}
		
		//scan.close();
		
		
		
	}
}
