package es.um.redes.P2P.PeerTracker.Server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import es.um.redes.P2P.util.FileInfo;
import es.um.redes.P2P.util.FileInfoSeeds;

public enum TrackerDatabase {

	db;
	
	private Map<String,FileInfoSeeds> files;

	TrackerDatabase() {
		this.files = new HashMap<String,FileInfoSeeds>();

	}

	public InetSocketAddress[] getSeeds(String fileHash) {
		FileInfoSeeds f = files.get(fileHash);
		if (f != null) {
			assert(f.seedList.size() > 0);
			return f.seedList.toArray(new InetSocketAddress[f.seedList.size()]);
		}
		else
			return new InetSocketAddress[0];
	}

	/**
	 * @return The list of ALL files in the tracker database
	 */
	public FileInfo[] getFileInfoList() {
		List<FileInfo> fileinfolist = new ArrayList<FileInfo>();
		Iterator<FileInfoSeeds> itr = files.values().iterator();
		while(itr.hasNext()) {
			FileInfoSeeds f = itr.next();
			fileinfolist.add(f);
		}
		FileInfo[] array = fileinfolist.toArray(new FileInfo[fileinfolist.size()]);
		return array;
	}

	public void addSeedToFileList(FileInfo[] fileList, InetSocketAddress clientSockAddr) {
		for(int i=0; i < fileList.length; i++) {
			FileInfoSeeds value = files.get(fileList[i].fileHash);
			if (value != null) { // File exists in database
				value.seedList.add(clientSockAddr);
				assert(value.fileName.equals(fileList[i].fileName));
				assert(value.fileSize == (fileList[i].fileSize));
				assert(files.get(fileList[i].fileHash).seedList.contains(clientSockAddr));
			}
			else {
				value = new FileInfoSeeds(fileList[i], clientSockAddr);
				files.put(fileList[i].fileHash, value);
			}
		}
	}
	
	public void removeSeedFromFileList(FileInfo[] fileList, InetSocketAddress seedId) {
		for(int i=0; i < fileList.length; i++) {
			FileInfoSeeds value = files.get(fileList[i].fileHash);
			if (value != null) { // File exists in database
				if (value.seedList.remove(seedId)) { // Seed was removed from seed list
					if (value.seedList.isEmpty()) { // Seed list becomes empty
						if (files.remove(fileList[i].fileHash) == null) {
							assert(false); // Key should exist and be removed
						}
						System.out.println("      Removed from database: "+value);
					}
					else { // More seeds remain, keep file in database 
						System.out.println("      Removed from seedlist: "+value);
					}
				}
				else { // Not removed from seed list
					System.err.println("      Peer not found in seedlist (not removed): "+value);				
				}
			} 
			else { // File not found in database
				System.err.println("      File not found in database : "+fileList[i]);				
			}
		}
	}
	public void disconnectPeer(InetSocketAddress clientSockAddr) {
		// Remove this peer from all seed lists
		Iterator<FileInfoSeeds> itr = files.values().iterator();
		while(itr.hasNext()) {
			FileInfoSeeds info = itr.next();
			if (info.seedList.remove(clientSockAddr)) {
				if (info.seedList.isEmpty()) {
					itr.remove();
					System.out.println("      Removed from database: "+info);
				}
				else {
					System.out.println("      Removed from seedlist: "+info);
				}
			}
			else {
				assert(info.seedList.isEmpty() == false);
			}
		}
	}

	public void connectPeer(InetSocketAddress clientSockAddr) {
		// Nothing to do: seed lists are updated upon disconnect
	}

	public void queryFromPeer(InetSocketAddress clientSockAddr) {
		// Nothing to do to process request, makeResponse will call getFileInfoList() 
	}

}
