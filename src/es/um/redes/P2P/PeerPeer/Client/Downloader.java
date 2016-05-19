package es.um.redes.P2P.PeerPeer.Client;

import java.io.*;
import java.net.*;

public class Downloader {
	
	// Aqui hay que implementar la sincronizacion entre los trozos que descargan los peer
	// para que no haya conflictos y varios peer no accedan al mismo trozo

	public void download(String peerAddress,int peerPort,String fileHash,long fileS, String folder) throws NumberFormatException, UnknownHostException, IOException
	{
		// Parameters are <hostname> <port>
		Socket socket = new Socket(peerAddress, peerPort);

		DownloaderThread dt = new DownloaderThread(this,socket,fileHash,fileS,folder);
		dt.start();
	}
}