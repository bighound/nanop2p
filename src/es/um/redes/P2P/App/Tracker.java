package es.um.redes.P2P.App;

import java.io.*;

import es.um.redes.P2P.PeerTracker.Server.TrackerThread;

public class Tracker {
	public static final int TRACKER_PORT = 4450;

	public static void main(String[] args) throws IOException {
		new TrackerThread("Tracker").start();
	}
}
