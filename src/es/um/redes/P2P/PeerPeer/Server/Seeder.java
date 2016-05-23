package es.um.redes.P2P.PeerPeer.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

//import es.um.redes.P2P.util.FileInfo;

/**
 * Servidor que se ejecuta en un hilo propio.
 * Creará objetos {@link SeederThread} cada vez que se conecte un cliente.
 */
public class Seeder implements Runnable {

	private static final int PORT = 8009;
	private static final int FINAL_PORT = 9000;
	
    private InetSocketAddress socketAddress;
    private ServerSocket seederSocket = null;
	String folder;
	

    public Seeder(String folder) {
		this.folder = folder;
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
   				new SeederThread(s, folder).start();
   			}
   		} catch (IOException e) {
   			// Do nothing
   		}
	}
    
   
    



	/**
     * Inicio del hilo del servidor.
     */
    public void init(){
		int puerto = PORT;
		while(puerto <= FINAL_PORT){
			socketAddress = new InetSocketAddress(puerto);
			try {
				seederSocket = new ServerSocket();
				seederSocket.bind(socketAddress);
				seederSocket.setReuseAddress(true);
				break;
			} catch (IOException e) {
				puerto++;
			}
		}
		// Inicia esta clase como un hilo
		new Thread(this).start();

    	System.out.println("Seeder running on port " +
    			this.socketAddress.getPort() + ".");
    }

	public int getPort(){
        return this.socketAddress.getPort();
    }

    /*public static void main(String[] args)
    {
    	Seeder server = new Seeder();
    	server.init();
    }*/
}