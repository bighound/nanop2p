package es.um.redes.P2P.PeerTracker.Message;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import es.um.redes.P2P.util.FileDigest;

/**
 * @author rtitos
 * 
 * Peer-tracker protocol data message, format "SeedInfo" 
    1b      20b       2b        4b     2b     4b    2b     
 +---------------------------------------------------------------------------+
 |opcode|  hash   |list.len  |  IP  | Port |  IP  | Port | ....              |
 +------+---------+----------+------+------+------+------+-------------------+
                             <--- seed 1---><---seed 2--->
                             <------- seed list  --------------------------->                                              
 */
public class MessageDataSeedInfo extends Message {

	/**
	 * Size of "seed" field: IPv4 (4 bytes) + port (recommended 4 bytes, short not enough)
	 */
	private static final int FIELD_SEED_BYTES = 4 + Integer.SIZE/8; 	// TODO: Replace 4 by "sizeof(IPv4 address)"  
 

	/**
	 * MessageP opcodes that use the SeedInfo format
	 */
	private static final Byte[] _dataseed_opcodes = {
			OP_GET_SEEDS, 
			OP_SEED_LIST };
	
	private String fileHash;
	private InetSocketAddress[] seedList;
	
	public MessageDataSeedInfo(byte opCode, InetSocketAddress[] seedList, String fileHash) {
		setOpCode(opCode);
		this.seedList = seedList;
		this.fileHash = fileHash;
		valid = true;
	}

	public MessageDataSeedInfo(byte[] buf) {
		if (fromByteArray(buf) == false) {
			throw new RuntimeException("Fallo al parsear mensaje de tipo DataSeed.");
		}
		valid = true;
	}

	/**
	 * Creates a byte array ready for sending a datagram packet, from a valid message of SeedInfo format 
	 */
	public byte[] toByteArray()
		{
			int length = FIELD_OPCODE_BYTES + FIELD_LONGLIST_BYTES + FIELD_FILEHASH_BYTES + seedList.length*FIELD_SEED_BYTES;
			
			ByteBuffer buf = ByteBuffer.allocate(length);

			// Opcode
			buf.put((byte)this.getOpCode());
			
			// List length
			buf.putShort((short)seedList.length);
			
			// File hash
			buf.put(FileDigest.getDigestFromHexString(fileHash));

			for(int i=0; i < seedList.length;i++) {
				// Seed list (IP+port)
				assert(seedList[i].getAddress().getAddress().length + 
					   Integer.SIZE/8 == FIELD_SEED_BYTES); 
				buf.put(seedList[i].getAddress().getAddress());
				buf.putShort((short)seedList[i].getPort());
			}

			return buf.array();
	}
	
	/**
	 * Creates a valid message of SeedInfo format, from the byte array of a received packet
	 */
	public boolean fromByteArray(byte[] array) {
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			// Opcode
			setOpCode(buf.get());
			// List length
			int listLength = buf.getShort();
			seedList = new InetSocketAddress[listLength];
			// File hash
			byte[] hasharray = new byte[FIELD_FILEHASH_BYTES];
			buf.get(hasharray, 0, hasharray.length);
			this.fileHash = new String(FileDigest.getChecksumHexString(hasharray));
			// Seed list
			byte[] iparray = new byte[FIELD_SEED_BYTES-Integer.SIZE/8];
			for(int i=0; i < seedList.length;i++) {
				buf.get(iparray, 0, iparray.length);
				InetAddress ip = InetAddress.getByAddress(iparray);
				int port = buf.getShort();
				seedList[i] = new InetSocketAddress(ip, port); 
			}
			valid = true;		
		} catch (RuntimeException e) {
			e.printStackTrace();
			assert(valid == false);
		} catch (UnknownHostException e) {
			// Invalid seed IP address
			e.printStackTrace();
			assert(valid == false);
		}
		return valid;
	}

	public String toString() {
		assert(valid);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" Type:"+this.getOpCodeString());
		strBuf.append(" ListLen:"+this.seedList.length);
		strBuf.append(" Hash:"+this.getFileHash());
		strBuf.append(" SeedInfoList:");
		for(int i=0; i < this.seedList.length;i++) {
			strBuf.append("["+this.seedList[i]+"]");
		}
		return strBuf.toString();
	}

	public InetSocketAddress[] getSeedList() {
		return this.seedList;
	}

	/**
	 * For checking opcode validity. 	
	 */
	private static final Set<Byte> dataseed_opcodes =
			Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_dataseed_opcodes)));

	protected void _check_opcode(byte opcode)
	{
		if (!dataseed_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " no es de tipo DataSeed.");
	}

	/* Getter, public to make it accessible to class TrackerThread::makeResponse 
	 * */
	public final String getFileHash() {
		assert(valid);
		return fileHash;
	}

}
