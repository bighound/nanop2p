package es.um.redes.P2P.PeerPeer.Server;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import es.um.redes.P2P.PeerPeer.Message.Message;
import jdk.management.resource.internal.inst.FileInputStreamRMHooks;

/**
 * Hilo que se ejecuta cada vez que se conecta un nuevo cliente.
 */
public class SeederThread extends Thread {
	private Socket socket = null;

	public SeederThread(Socket socket) {
		super("SeederThread");
		this.socket = socket;
	}

	/** FunciÃ³n de los hilos que atienden a los clientes.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			// Recibo de datos
			Message msg = new Message();
			byte[] buffer = new byte[150];
			InputStream is = socket.getInputStream();
			is.read(buffer);
			String s = new String(buffer, 0, buffer.length);
			//BufferedReader br = new BufferedReader(new InputStreamReader(is));
			//br.toString();
			
			//is.read();
			//String s = new String(buffer, 0, buffer.length);
			System.out.println("Ha recibido(el seeder): " + s );
			
			
			
			OutputStream os = socket.getOutputStream();
			String m;
			switch (msg.parseMessage(s)) {
			case 1: //request_chunk
                //requestChunks(contenido);
				
				/*File dir = new File(folder);
				// Creamos el archivo destino
				File file = new File(folder +name);
				// Abrimos los steams
				OutputStream out = new FileOutputStream(file);*/
				
				/*PrintWriter stdout = new PrintWriter(new FileWriter("archivosSalida"));
				stdout.println("Hola amiguitos miuos");
				stdout.close();*/
				
				File f = new File("C:\\Users\\Bighound\\Desktop\\Apuntes\\REDES\\NanoP2P\\nanop2p-master\\nanop2p-master\\sharedp2p\\peer1\\a.txt");
				FileInputStream fis = new FileInputStream(f);
				
				long filel = f.length();
				byte data[] = new byte[(int) filel];
				fis.read(data);
				
				
				socket.getOutputStream().write(data);
				fis.close();
				
				 m = Message.createMessageSend("Aqui iría el chunk");
				//socket.getOutputStream().write(m.getBytes());
				System.out.println("El seeder ha enviado un send_chunk: "+ m);
                break;
           /* case "send_chunk": 				 
            	tipo = 2;
                //sendChunk(contenido);
                break;*/
            case 3:  // all_chunks_received Si me envian un all chunks received entonces le envío un mensaje de correcto:
            	 m = "OK todo correcto";
				socket.getOutputStream().write(m.getBytes());
				System.out.println("El seeder ha enviado un OK: "+ m);
                // SeÃ±al todos recibidos
                break;
            case 4:   //File not found
            	m = Message.createMessageNot("Este es un hash");
				socket.getOutputStream().write(m.getBytes());
				System.out.println("File no encontrado: "+ m);
                //fileNotFound(contenido);
                break;
            default:
                System.out.println("Mensaje con formato no correcto");
                break;
			}
			//System.out.println("Ha enviado(el seeder): " + fileHash);
			
			
			/*OutputStream os = socket.getOutputStream();
			Message m = new Message();
			m.createMessageSend(s);
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeChars(Message.createMessageSend(s));
			//String m = Message.createMessageRequest(fileHash, 1);
			//socket.getOutputStream().write(m.toString());
			System.out.println("Ha enviado: " + m);
			//socket.getOutputStream().write(m.getBytes());
*/

			
			// Interpretacion del protocolo
			// Primera clase, leer el hash de un fichero y devolver el hash.
			
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}