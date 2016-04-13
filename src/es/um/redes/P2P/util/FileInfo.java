package es.um.redes.P2P.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileInfo {
	public String fileHash;
	public String fileName;
	public long fileSize;
	
	public FileInfo() {
	}
	
	public FileInfo(String hash, String name, long size) {
		fileHash = hash;
		fileName = name;
		fileSize = size;
	}
	
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" FileHash:"+fileHash);
		strBuf.append(String.format(" FileSize: %1$9s",fileSize));
		strBuf.append(" FileName: "+fileName);
		return strBuf.toString();
	}

	/**
	 * Scans the given directory and returns an array of FileInfo objects, one for
	 * each file recursively found in the given folder and its subdirectories.
	 * @param sharedFolderPath The folder to be scanned
	 * @return An array of file metadata (FileInfo) of all the files found 
	 */
	public static FileInfo[] loadFilesFromFolder(String sharedFolderPath) {
		File folder = new File(sharedFolderPath);
		
		Map<String,FileInfoPath> files = loadFileMapFromFolder(folder);
		
	    FileInfo[] fileinfoarray = new FileInfo [files.size()];
		Iterator<FileInfoPath> itr = files.values().iterator();
		int numFiles=0;
		while(itr.hasNext()) {
			fileinfoarray[numFiles++] = itr.next();
		}
		return fileinfoarray;
	}

	/**
	 * Scans the given directory and returns a map of <filehash,FileInfo> pairs. 
	 * @param folder The folder to be scanned
	 * @return A map of the metadata (FileInfo) of all the files recursively found 
	 * in the given folder and its subdirectories.
	 */
	public static Map<String,FileInfoPath> loadFileMapFromFolder(final File folder) {
		Map<String,FileInfoPath> files = new HashMap<String,FileInfoPath>();
		scanFolderRecursive(folder, files);
		return files;
	}
		
	private static void scanFolderRecursive(final File folder, Map<String,FileInfoPath> files) {
		if (folder.exists() == false) {
			System.err.println("scanFolder cannot find folder "+folder.getPath());
			return;
		}
		if (folder.canRead() == false) {
			System.err.println("scanFolder cannot access folder "+folder.getPath());
			return;
		}
		
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            scanFolderRecursive(fileEntry, files);
	        } else {
	            String fileName = fileEntry.getName();
	            String filePath = fileEntry.getPath();
	    		String fileHash = FileDigest.getChecksumHexString(FileDigest.computeFileChecksum(filePath)); 
	    		long fileSize = fileEntry.length();
	    		files.put(fileHash, new FileInfoPath(fileHash, fileName, fileSize, filePath));
	        }
	    }
	}
}
