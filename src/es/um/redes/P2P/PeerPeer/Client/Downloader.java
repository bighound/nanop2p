package es.um.redes.P2P.PeerPeer.Client;

//import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
import es.um.redes.P2P.util.FileInfo;

import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

public class Downloader {

	public final static int CHUNK_SIZE = 1024;
	boolean [] chunkSeen;

	// Aqui hay que implementar la sincronizacion entre los trozos que descargan los peer
	// para que no haya conflictos y varios peer no accedan al mismo trozo

	public void download(InetSocketAddress [] dirs, FileInfo file, String folder) throws NumberFormatException, IOException
	{
		Semaphore mutex = new Semaphore(1);

		// Comprobar que chunk=0
		int nChunks = (int) file.fileSize/CHUNK_SIZE;
		if (file.fileSize%CHUNK_SIZE!=0) nChunks++;

		// Array de booleanos para que los hilos controlen que chunks pueden descargar
		chunkSeen = new boolean [nChunks];
		for (int i = 0; i < chunkSeen.length; i++) {
			chunkSeen[i] = false;
		}

		// Creamos tantos sockets como peers existan con el archivo deseado
		Socket [] sockets = new Socket [dirs.length];
		for (int i = 0; i < sockets.length; i++) {
			sockets[i] = new Socket(dirs[i].getAddress().toString().substring(1), dirs[i].getPort());
		}

		// Creamos los downloaderThread correspondientes
		DownloaderThread [] downThreads = new DownloaderThread [sockets.length];
		for (int i = 0; i < downThreads.length; i++) {
			downThreads[i] = new DownloaderThread(this,sockets[i],file,folder, mutex);
		}

		// Iniciamos los hilos
		for (DownloaderThread downThread : downThreads) {
			downThread.start();
		}

		// Esperamos a que los hilos terminen
		try {
			for (DownloaderThread downThread : downThreads) {
				downThread.join();
			}
		} catch (InterruptedException e) {
			System.out.println("Error en la ejecucion de un DownloaderThread");
		}

	}


}