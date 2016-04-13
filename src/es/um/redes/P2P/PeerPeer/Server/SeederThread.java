package es.um.redes.P2P.PeerPeer.Server;
import java.io.InputStream;
import java.net.Socket;

/**
 * Hilo que se ejecuta cada vez que se conecta un nuevo cliente.
 */
public class SeederThread extends Thread {
	private Socket socket = null;

	public SeederThread(Socket socket) {
		super("SeederThread");
		this.socket = socket;
	}

	/** Función de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// Recibo de datos
			
			InputStream is = socket.getInputStream();			
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}