package es.um.redes.P2P.PeerPeer.Client;

//import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
import es.um.redes.P2P.util.FileInfo;

import java.io.*;
import java.net.*;

public class Downloader {

	public final static int CHUNK_SIZE = 1024;
	
	// Aqui hay que implementar la sincronizacion entre los trozos que descargan los peer
	// para que no haya conflictos y varios peer no accedan al mismo trozo

	public void download(String peerAddress, int peerPort, FileInfo file, String folder) throws NumberFormatException, UnknownHostException, IOException
	{
		// Parameters are <hostname> <port>
		Socket socket = new Socket(peerAddress, peerPort);

		// Comprobar que chunk=0
		int nChunks = (int) file.fileSize/CHUNK_SIZE;
		if (file.fileSize%CHUNK_SIZE!=0) nChunks++;

		// Ya tenemos el numero de chunks en los que se divide el archivo deseado
		// Hay que crear tantos downloaderThread como seeders existan.

		// Cada uno se conectara al seed correspondiente y le pedira un chunk
		// Recibira un chunk, este chunk sera el que hay en la posicion chunk_number*1024





		DownloaderThread dt = new DownloaderThread(this,socket,file,folder,nChunks);
		dt.start();



	}


}