package es.um.redes.P2P.PeerPeer.Message;

/**
 * Created by Jorge Gallego Madrid on 12/05/2016.
 */
public enum MessageCode {
    REQUEST_CHUNK,
    SEND_CHUNK,
    ALL_CHUNKS_RECEIVED,
    FILE_NOT_FOUND;
}
