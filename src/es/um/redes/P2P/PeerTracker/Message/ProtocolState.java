package es.um.redes.P2P.PeerTracker.Message;

public enum ProtocolState
{
	NOT_CONNECTED,
//	CONNECTING,
	PUBLISHING,
	ONLINE,
	IN_QUERYFILES,
	IN_ADDSEED,
	IN_GETSEEDS,
	DOWNLOADING,
	IN_DISCONNECT,
	ERROR_DOWNLOAD,
	DISCONNECTED;	
};

