package es.um.redes.P2P.PeerPeer.Message;


import java.util.regex.*;
import java.util.regex.Pattern;

public class Message {

    // Expresiones regulares
    private String expression = "<(\\w+?)>(.*?)</\1>";
    private String message = "<(message)>(.*?)</message>";
    private String operation = "<(operation)>(.*?)</operation>";
    private String chunks = "<(chunks)>(.*?)</chunks>";
    private String send_chunk = "<(send_chunk)>(.*?)</send_chunk>";




    public boolean parseMessage(String m){

        Pattern pat = Pattern.compile(expression);
        Matcher mat = pat.matcher(m);
        if (mat.find()){
            System.out.println("He encontrado un match");
        }
        String tipoMensaje = mat.group(1);
        String contenido = mat.group(2);

        return true;
    }


}
