package es.um.redes.P2P.PeerPeer.Client;

//import es.um.redes.P2P.PeerPeer.MessageP.MessageP;
import es.um.redes.P2P.util.FileInfo;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.concurrent.Semaphore;


public class Downloader {

	public final static int CHUNK_SIZE = 1024;
	boolean [] chunkSeen;
    int [] chunkPerThread;
    

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

        System.out.println("Descargando el archivo desde " + sockets.length + " seeders");
        // Creamos los downloaderThread correspondientes
		DownloaderThread [] downThreads = new DownloaderThread [sockets.length];
        chunkPerThread = new int [sockets.length];
		for (int i = 0; i < downThreads.length; i++) {
			downThreads[i] = new DownloaderThread(this,sockets[i],file,folder, mutex, i);
            chunkPerThread[i] = 0;
		}


		long inicio = System.currentTimeMillis();
		// Iniciamos los hilos
		for (DownloaderThread downThread : downThreads) {
			downThread.start();
		}
		long fin = 0;
		// Esperamos a que los hilos terminen
		try {
			for (DownloaderThread downThread : downThreads) {
				downThread.join();
			}
			fin = System.currentTimeMillis();
		} catch (InterruptedException e) {
			System.out.println("Error en la ejecucion de un DownloaderThread");
		}
        System.out.println("-----------------Descarga completada-----------------");
        long diferencia = fin - inicio;
        double segundos = (double)diferencia/1000.0;
        DecimalFormat df = new DecimalFormat("#.##");
        double velocidad = (file.fileSize/1024/1024)/segundos;
        System.out.println("La descarga ha tardado " + segundos + " s a " + df.format(velocidad) + " MB/s");
        System.out.println("Ha descargado:");
        for (int i = 0; i < chunkPerThread.length; i++) {
            System.out.println("\t" + chunkPerThread[0] + " chunks del seeder " + dirs[i].getAddress().toString().substring(1)
                                + ":" + dirs[i].getPort());
        }
    }
}