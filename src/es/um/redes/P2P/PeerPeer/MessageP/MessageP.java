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

public abstract class MessageP {

    // Expresiones regulares

    // Expresion regular que hace match con un mensaje entero
    // Grupo 1: message
    // Grupo 2: operation
    // Grupo 3: tipo de operacion
    // Grupo 4: contenido del mensaje a partir de la operacion
    //private static String mensaje = "<(message)>\\s*?<(operation)>(.*?)</\\2>((.|\\s)*?)</\\1>";
	private static String mensaje = "<(message)><(operation)>(.*?)</\\2>(.*?)</\\1>";
    // Expresion regular que hace match con un hash
    // Grupo 2: hash number
    // Grupo 4: chunk_number
    private static String reqChunks = "<(hash)>(.*?)</\\1>\\s*?<(chunk)>(.*?)</\\3>";

    // Expresion regular que hace match con un numero de chunk
    // Grupo 2: chunk
    // Falta chunk_number .ya no falta
    private static String sndChunk = "<(send_chunk)>(.*?)</\\1><(chunk)>(.*?)</\\3>";  

    // Expresion regular que hace match con un chunk
    // Grupo 2: hash_number
    //private static String fileNot = "<(send_chunk)>(.*?)</\\1>";

    private static String hash;
    private static int chunkNumber;
    private static byte[] chunk;
    private static String msg;
    private static String chunkToString;


	public static String getHash() {
		return hash;
	}

	public static byte[] getChunk() {
		return chunk;
	}

	public static int getChunkNumber() {
		return chunkNumber;
	}


	//private static void requestChunks(String s){}

    public static void sendChunk(String s){
        
    }

   /* private static void fileNotFound(String s){ ************************************************************************  Falta esto
        Pattern p = Pattern.compile(fileNot);
        Matcher m = p.matcher(s);
        m.find();
        hash = m.group(2);
    }*/


    public static String createMessageRequest (String hash, int chunk){
        String chunkS = chunk + "";
        msg = ("<message><operation>request_chunk</operation><hash>" + hash + "</hash><chunk>" +
                                    chunkS + "</chunk></message>\n");
        return msg;
    }

    //  

    public static String createMessageSend (byte[] data,int chunkNumber){//byte[] data
    	String chunk;
    	chunk=DatatypeConverter.printBase64Binary(data);
    	
        msg = ("<message><operation>send_chunk</operation><send_chunk>" + chunk + "</send_chunk><chunk>" +
                                    chunkNumber + "</chunk></message>\n");
        return msg;
    }

    public static String createMessageAll (){
        msg = ("<message><operation>all_chunks_received</operation></message>\n");
        return msg;
    }

    public static String createMessageNot (String hash){
        msg =("<message><operation>file_not_found</operation><hash>" + hash + "</hash></message>\n");
        return msg;
    }
    static Pattern p;
    static Matcher mat;
    
    public static int parseResponse(String m){
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
                //System.out.println("Esto es un requestChunk");
                
                return 1;
                
            case "send_chunk":
            	 p = Pattern.compile(sndChunk);
                mat = p.matcher(contenido);
                mat.find();  
                chunkNumber = Integer.parseInt(mat.group(4));
                chunkToString = mat.group(2);
                chunk=DatatypeConverter.parseBase64Binary(chunkToString);
                //System.out.println("efectivamente se ha encontrado con el chunk: "+chunkToString+"y el chunk number"+chunkNumber);
                
                //sendChunk(contenido);
                return 2;
                
                
            case "all_chunks_received":
            	System.out.println("-----------------All chunks have been received-----------------");
            	return 3;
            case "file_not_found":
                
                break;
            default:
                System.out.println("Mensaje con formato no correcto");
                return 5;
        }
        return -1;
    }



}


/*
    // Expresion regular que hace match con un hash
    // Grupo 2: hash number
    // Grupo 4: chunk_number
    private String reqChunks = "<(hash)>(.*?)</\\1>\\s*?<(chunk)>(.*?)</\\3>";

    // Expresion regular que hace match con un numero de chunk
    // Grupo 2: chunk
    private String sndChunk = "<(send_chunk)>(.*?)</\\1>";

    // Expresion regular que hace match con un chunk
    // Grupo 2: hash_number
    private String fileNot = "<(send_chunk)>(.*?)</\\1>";


    private void requestChunks(String s){
        Pattern p = Pattern.compile(reqChunks);
        Matcher m = p.matcher(s);
        m.find();
        String fileHash = m.group(2);
        String chunkNumber = m.group(4);


    }

    private void sendChunk(String s){
        Pattern p = Pattern.compile(sndChunk);
        Matcher m = p.matcher(s);
        m.find();
        String chunk = m.group(2);
    }

    private void fileNotFound(String s){
        Pattern p = Pattern.compile(fileNot);
        Matcher m = p.matcher(s);
        m.find();
        String fileHash = m.group(2);
    }
	*/
    
/*


    public int parseMessage(String m){
    	
    	int tipo = 0;
        Pattern pat = Pattern.compile(mensaje);
        Matcher mat = pat.matcher(m);
        if (!mat.find()){
            System.out.println("Mensaje con formato no correcto");
        }
        String tipoOperacion = mat.group(3);
        String contenido = mat.group(4);

        switch (tipoOperacion){
            case "request_chunk":
                requestChunks(contenido);
            	tipo=1;
                break;
            case "send_chunk":
            	tipo = 2;
                sendChunk(contenido);
                break;
            case "all_chunks_received":
            	tipo = 3;
                // Señal todos recibidos
                break;
            case "file_not_found":
            	tipo = 4;
                //fileNotFound(contenido);
                break;
            default:
                System.out.println("Mensaje con formato no correcto");
                tipo = 5;
                break;
        }
        return tipo;
       // return true;
    }


}*/
