package es.um.redes.P2P.PeerTracker.Message;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import es.um.redes.P2P.util.FileDigest;
import es.um.redes.P2P.util.FileInfo;

/**
 * @author rtitos
 * 
 * Peer-tracker protocol data message, format "FileInfo" 
 * 
    1b      2b      2b       20b       8b           4b        0 .. long.filename-1   
 +--------------------------------------------------------------------------------+
 |opcode| port  | list.len| hash   | filesize  |filename.len | filename           |
 +------+-------+---------+--------+-----------+-------------+--------------------+
                          <---------------- file info  --------------------------->
                 
 */
public class MessageDataFileInfo extends Message {

	/**
	 * Size of "filesize" field: long (8 bytes)
	 */
	private static final int FIELD_FILESIZE_BYTES = Long.SIZE / 8;  
	/**
	 * Size of "filename.len" field: int (4 bytes)
	 */
	private static final int FIELD_FILENAMELEN_BYTES = Integer.SIZE / 8;

	/**
	 * Message opcodes that use the FileInfo format
	 */
	private static final Byte[] _datafile_opcodes = {
			OP_ADD_SEED, 
			OP_FILE_LIST,
			OP_REMOVE_SEED};
	
	/**
	 * The port number where this seeder listens
	 */
	private int seederPort;

	/**
	 * The list of "FileInfo" entries contained in the message.
	 */
	private FileInfo[] fileList;
	
	/**
	 * Constructor used by client peer when creating message requests for sending.
	 * @param opCode Message type
	 * @param fileList List of files
	 */
	public MessageDataFileInfo(byte opCode, int port, FileInfo[] fileList) {
		setOpCode(opCode);
		this.seederPort = port;
		this.fileList = fileList;
		valid = true;
		// Constructor used when creating ADD_SEED/QUERY_FILES requests (port info required)
	}

	public MessageDataFileInfo(byte opCode, FileInfo[] fileList) {
		setOpCode(opCode);
		this.fileList = fileList;
		valid = true;
		// This constructor is only used when creating FILE_LIST responses (no port info required)
		assert(opCode == OP_FILE_LIST);
	}

	/**
	 * Constructor used by tracker when creating message response after receiving
	 * @param buf
	 */
	public MessageDataFileInfo(byte[] buf) {
		if (fromByteArray(buf) == false) {
			throw new RuntimeException("Fallo al parsear mensaje de tipo DataFile.");
		}
		else {
			assert(valid);
		}
	}

	/**
	 * Creates a byte array ready for sending a datagram packet, from a valid message of FileInfo format 
	 */
	public byte[] toByteArray()
	{
		int fileInfoBytes=0;
		// FileInfo fields with constant size: hash, filesize and filename length
		fileInfoBytes += fileList.length*(FIELD_FILEHASH_BYTES + FIELD_FILESIZE_BYTES + 
				FIELD_FILENAMELEN_BYTES);

		for(int i=0; i < fileList.length; i++) {
			// Variable filename length
			fileInfoBytes += fileList[i].fileName.getBytes().length;
		}
		int byteBufferLength = FIELD_OPCODE_BYTES + FIELD_LONGLIST_BYTES + FIELD_PORT_BYTES + fileInfoBytes;
		
		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		// Opcode
		buf.put((byte)this.getOpCode());
		
		// Seeder port
		buf.putShort((short)this.getPort());
		
		// List length
		buf.putShort((short)fileList.length);

		for(int i=0; i < fileList.length;i++) {
			// File hash
			buf.put(FileDigest.getDigestFromHexString(fileList[i].fileHash));

			// File size
			buf.putLong(fileList[i].fileSize);
			
			// Filename length 
			buf.putInt(fileList[i].fileName.length());
			
			// Filename
			buf.put(fileList[i].fileName.getBytes());
		}

		return buf.array();
	}

	/**
	 * Creates a valid message of FileInfo format, from the byte array of the received packet
	 */
	public boolean fromByteArray(byte[] array) {
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			// Opcode
			setOpCode(buf.get());
			// Seeder port
			setPort(buf.getShort());
			// List length
			int listLength = buf.getShort();

			this.fileList = new FileInfo[listLength];
			byte[] hasharray = new byte[FIELD_FILEHASH_BYTES];
			for(int i=0; i < listLength;i++) {
				FileInfo info = new FileInfo();
				// File hash
				buf.get(hasharray, 0, FIELD_FILEHASH_BYTES);
				info.fileHash = new String(FileDigest.getChecksumHexString(hasharray));
				// File size
				info.fileSize = buf.getLong();
				// Filename length
				int filenameLen = buf.getInt();				
				byte[] filenamearray = new byte[filenameLen];
				// Filename
				buf.get(filenamearray, 0, filenameLen);				
				info.fileName = new String(filenamearray);
				this.fileList[i] = info;
			}
			valid = true;		
		} catch (RuntimeException e) {
			e.printStackTrace();
			assert(valid == false);
		}
		return valid;
	}	


	public int getPort() {
		return this.seederPort;
	}

	private void setPort(int port) {
		this.seederPort = port;
	}

	public FileInfo[] getFileList() {
		return this.fileList;
	}
	
	public String toString() {
		assert(valid);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("Tipo:"+this.getOpCodeString());
		strBuf.append(" LongLista:"+this.fileList.length);
		strBuf.append(" SeedPort:"+this.getPort());
		strBuf.append(" FileInfoList:");
		for(int i=0; i < this.fileList.length;i++) {
			strBuf.append(System.lineSeparator()+"      ["+this.fileList[i]+"]");
		}
		return strBuf.toString();
	}

	/**
	 * For checking opcode validity. 	
	 */
	private static final Set<Byte> datafile_opcodes =
		Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_datafile_opcodes)));

	protected void _check_opcode(byte opcode)
	{
		if (!datafile_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " no es de tipo DataFile.");
	}
}
