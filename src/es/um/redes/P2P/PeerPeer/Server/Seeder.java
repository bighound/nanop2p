package es.um.redes.P2P.PeerPeer.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor que se ejecuta en un hilo propio.
 * Creará objetos {@link SeederThread} cada vez que se conecte un cliente.
 */
public class Seeder implements Runnable {

	public static final int PORT = 8009;
	
    private InetSocketAddress socketAddress;
    private ServerSocket seederSocket = null;

    private Seeder(InetSocketAddress a)
    {
    	this.socketAddress = a;
    }

    /** 
	 * Función del hilo principal del servidor. 	
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
   		try {
   			while (true)
   			{
   				// Espera una conexión del cliente
   				// El accept retorna un nuevo socket para hablar directamente
   				// con el nuevo cliente conectado
   				Socket s = seederSocket.accept();
   				
   				// Inicia el hilo de servicio al cliente recién conectado,
   				// enviándole el estado general del servidor y el socket de 
   				// este cliente
   				new SeederThread(s).start();
   			}
   		} catch (IOException e) {
   			// Do nothing
   		}
	}
    
    /**
     * Inicio del hilo del servidor.
     */
    public void init()
    {
        try {
        	// Crea el socket de servidor y lo liga a la dirección
        	// local y al puerto especificado en socketAddress
            seederSocket = new ServerSocket();
            seederSocket.bind(socketAddress);
            seederSocket.setReuseAddress(true);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " 
            		+ socketAddress.getPort() + ".");
            System.exit(-1);
        }

        // Inicia esta clase como un hilo
    	new Thread(this).start();
    	
    	System.out.println("Seeder running on port " +
    			socketAddress.getPort() + ".");
    }

    public static void main(String[] args)
    {
    	Seeder server = new Seeder(new InetSocketAddress(PORT));
    	server.init();
    }
}