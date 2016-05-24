package es.um.redes.P2P.PeerPeer.MessageP;
/*
    REQUEST_CHUNK: petición del chunk que quiere el cliente (peer-cliente)
    <message>
	    <operation>request_chunk</operation>
		<hash>hash</hash>
		<chunk>chunk_number</chunk>
	</message>

	SEND_CHUNK: envío de un chunk del cliente al servidor (peer-servidor)
	<message>
		<operation>send_chunk</operation>
		<chunk>chunk_number</chunk>
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
import javax.xml.bind.DatatypeConverter;

import static es.um.redes.P2P.PeerPeer.MessageP.MessageCode.*;

public class MessageP {

    private MessageCode codigo;
    private String hash;
    private int chunkNumber;
    private byte[] chunk;


    // Constructor
    public MessageP(){
        this.codigo = INVALID_CODE;
        this.hash=null;
        this.chunkNumber = -1;
        this.chunk = null;
    }

    public MessageP (MessageCode codigo, String hash, int chunkNumber, byte[] chunk){
        switch (codigo){
            case REQUEST_CHUNK:
                this.codigo = REQUEST_CHUNK;
                this.hash = hash;
                this.chunkNumber = chunkNumber;
                this.chunk = null;
                break;
            case SEND_CHUNK:
                this.codigo = SEND_CHUNK;
                this.hash = null;
                this.chunkNumber = chunkNumber;
                this.chunk = chunk;
                break;
            case ALL_CHUNKS_RECEIVED:
                this.codigo = ALL_CHUNKS_RECEIVED;
                this.hash=null;
                this.chunkNumber = -1;
                this.chunk = null;
                break;
            case FILE_NOT_FOUND:
                this.codigo = FILE_NOT_FOUND;
                this.hash=hash;
                this.chunkNumber = -1;
                this.chunk = null;
                break;
        }
    }

    @Override
    public String toString() {
        String salida = "";
        switch (this.codigo){
            case REQUEST_CHUNK:
                String chunkS = this.chunkNumber + "";
                salida = ("<message><operation>request_chunk</operation><hash>" + this.hash + "</hash><chunk>" +
                        chunkS + "</chunk></message>\n");
                break;
            case SEND_CHUNK:
                salida = ("<message><operation>send_chunk</operation><send_chunk>"
                        + DatatypeConverter.printBase64Binary(this.chunk) + "</send_chunk><chunk>"
                        + chunkNumber + "</chunk></message>\n");
                break;
            case ALL_CHUNKS_RECEIVED:
                salida = ("<message><operation>all_chunks_received</operation></message>\n");
                break;
            case FILE_NOT_FOUND:
                salida = ("<message><operation>file_not_found</operation><hash>" + this.hash + "</hash></message>\n");
                break;
        }
        return salida;
    }

    public String getHash() {
		return this.hash;
	}

	public byte[] getChunk() {
		return this.chunk;
	}

	public int getChunkNumber() {
        return this.chunkNumber;
	}
    
    public MessageCode parseResponse(String m){
        // Expresion regular que hace match con un mensaje entero
        // Grupo 1: message
        // Grupo 2: operation
        // Grupo 3: tipo de operacion
        // Grupo 4: contenido del mensaje a partir de la operacion
        //private static String mensaje = "<(message)>\\s*?<(operation)>(.*?)</\\2>((.|\\s)*?)</\\1>";
        String mensaje = "<(message)><(operation)>(.*?)</\\2>(.*?)</\\1>";
        // Expresion regular que hace match con un hash
        // Grupo 2: hash number
        // Grupo 4: chunk_number
        String reqChunks = "<(hash)>(.*?)</\\1>\\s*?<(chunk)>(.*?)</\\3>";
        // Expresion regular que hace match con un numero de chunk
        // Grupo 2: chunk
        // Falta chunk_number .ya no falta
        String sndChunk = "<(send_chunk)>(.*?)</\\1><(chunk)>(.*?)</\\3>";

        Pattern p;
        Pattern pat = Pattern.compile(mensaje);
        Matcher mat = pat.matcher(m);
        if (!mat.find()){
            System.out.println("Mensaje con formato no correcto");
        }
        String tipoOperacion = mat.group(3);
        String contenido = mat.group(4);

        switch (tipoOperacion){
            case "request_chunk":
            	p = Pattern.compile(reqChunks);
                mat = p.matcher(contenido);
                mat.find();
                hash = mat.group(2);
                chunkNumber = Integer.parseInt(mat.group(4));
                
                return REQUEST_CHUNK;
                
            case "send_chunk":
                p = Pattern.compile(sndChunk);
                mat = p.matcher(contenido);
                mat.find();
                chunkNumber = Integer.parseInt(mat.group(4));
                String chunkToString = mat.group(2);
                chunk=DatatypeConverter.parseBase64Binary(chunkToString);
                return SEND_CHUNK;

            case "all_chunks_received":
            	return ALL_CHUNKS_RECEIVED;
            case "file_not_found":
                return FILE_NOT_FOUND;
            default:
                System.out.println("Mensaje con formato no correcto");
                return INVALID_CODE;
        }
    }
}