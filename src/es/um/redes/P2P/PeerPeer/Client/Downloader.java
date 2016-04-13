package es.um.redes.P2P.PeerPeer.Client;

import java.io.*;
import java.net.*;

public class Downloader {
	

	public void download(String peerAddress,int peerPort,String fileHash) throws NumberFormatException, UnknownHostException, IOException
	{
		// Parameters are <hostname> <port>
		Socket socket = new Socket(peerAddress, peerPort);

		DownloaderThread dt = new DownloaderThread(this,socket,fileHash);
		dt.start();
	}
}