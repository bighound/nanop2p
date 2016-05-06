package es.um.redes.P2P.PeerPeer.Message;


/*
    REQUEST_CHUNK: petición del chunk que quiere el cliente (peer-cliente)
	<message>
		<operation>request_chunks</operation>
		<hash>hash</hash>
		<chunk>chunk_number</chunk>
	</message>

	SEND_CHUNK: envío de un chunk del cliente al servidor (peer-servidor)
	<message>
		<operation>send_chunk</operation>
		<send_chunk>chunk</send_chunk>
	</message>

	ALL_CHUNKS_RECEIVED: mensaje para indicarle al servidor que ya hemos recibido todos los chunks
	y vamos a cerrar la conexión.
	<message>
		<operation>all_chunks_received</operation>
	</message>

	FILE_NOT_FOUND: mensaje
	<message>
		<operation>file_not_found</operation>
		<hash>hash</hash>
	</message>
*/



import java.util.regex.*;
import java.util.regex.Pattern;

public class Message {

    // Expresion regular que hace match con un mensaje entero
    // Grupo 1: message
    // Grupo 2: operation
    // Grupo 3: tipo de operacion
    // Grupo 4: contenido del mensaje a partir de la operacion
    private String mensaje = "<(message)>\\s*?<(operation)>(.*?)</\\2>((.|\\s)*?)</\\1>";

    // Expresion regular que hace match con un hash
    // Grupo 1: hash
    // Grupo 2: identificador hash
    private String hash = "<(hash)>(.*?)</\\1>";

    // Expresion regular que hace match con un numero de chunk
    // Grupo 1: chunk
    // Grupo 2: numero de chunk
    private String chunk = "<(chunk)>(.*?)</\\1>";

    // Expresion regular que hace match con un chunk
    // Grupo 1: send_chunk
    // Grupo 2: datos del chunk
    private String send_chunk = "<(send_chunk)>(.*?)</\\1>";


    public void requestChunks(String s){
        Pattern p = Pattern.compile(hash);
        Matcher m = p.matcher(s);
        m.find();
        String fileHash = m.group(1);
        p = Pattern.compile(chunk);
        m = p.matcher(s);
        m.find();


    }
    public void sendChunk(String s){
        Pattern p = Pattern.compile(hash);
        Matcher m = p.matcher(s);
        if (!m.find()){
            System.out.println("Mensaje con formato no correcto");
        }
    }
    public void allChunksReceived(String s){
        Pattern p = Pattern.compile(hash);
        Matcher m = p.matcher(s);
        if (!m.find()){
            System.out.println("Mensaje con formato no correcto");
        }
    }
    public void fileNotFound(String s){
        Pattern p = Pattern.compile(hash);
        Matcher m = p.matcher(s);
        if (!m.find()){
            System.out.println("Mensaje con formato no correcto");
        }
    }



    public boolean parseMessage(String m){

        Pattern pat = Pattern.compile(mensaje);
        Matcher mat = pat.matcher(m);
        if (!mat.find()){
            System.out.println("Mensaje con formato no correcto");
        }
        String tipoOperacion = mat.group(3);
        String contenido = mat.group(4);

        switch (tipoOperacion){
            case "request_chunks":
                requestChunks(contenido);
                break;
            case "send_chunk":
                sendChunk(contenido);
                break;
            case "all_chunks_received":
                sendChunk(contenido);
                break;
            case "file_not_found":
                sendChunk(contenido);
                break;
            default:
                System.out.println("Mensaje con formato no correcto");
                break;
        }

        return true;
    }


}
