
package es.um.redes.P2P.PeerTracker.Message;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import es.um.redes.P2P.util.FileDigest;
import es.um.redes.P2P.util.FileInfo;

/**
* Abstract class that models peer-tracker messages without a specific format
*
* @author rtitos
*
*/

public abstract class Message
{
	/**
	 * Maximum size of the buffer used to received packets 
	 */
	public static final int MAX_UDP_PACKET_LENGTH = 65000;

	/**
	 * Size of "opcode" field: byte (1 bytes)
	 */
	protected static final int FIELD_OPCODE_BYTES = 1;

	/**
	 * Size of "list-len" field: short (2 bytes)
	 */
	protected static final int FIELD_LONGLIST_BYTES = Short.SIZE / 8;
	/**
	 * Size of "hash" field(s) used by subclasses (160 bits in SHA-1, 20 bytes)
	 */
	protected static final int FIELD_FILEHASH_BYTES = FileDigest.getFileDigestSize();
	/**
	 * Size of "port" field: int (4 bytes)
	 */
	protected static final int FIELD_PORT_BYTES = Short.SIZE / 8;  

	/**
	 * Opcodes in the peer-tracker protocol of nanoP2P
	 */
	public static final byte INVALID_OPCODE = 0;
	public static final byte OP_ADD_SEED = 1;
	public static final byte OP_ADD_SEED_ACK = 2;
	public static final byte OP_QUERY_FILES = 3;
	public static final byte OP_FILE_LIST = 4;
	public static final byte OP_GET_SEEDS = 5;
	public static final byte OP_SEED_LIST = 6;
	public static final byte OP_REMOVE_SEED = 7;
	public static final byte OP_REMOVE_SEED_ACK = 8;
	
	/**
	 * Message opcode.     
	 */
	private byte opCode;

	/*
	 * Validity flag used for correctness check (asserts)     
	 */
	protected boolean valid; // Flag set by fromByteArray upon success 


	/*
	 * Abstract methods whose implementation depends on the message format.     
	 */
	public abstract boolean fromByteArray(byte[] buf);

	public abstract byte[] toByteArray();

	public abstract String toString();

	/**
	 * Default class constructor, creates "empty" message in invalid state 
	 */
	public Message()
	{
		opCode = INVALID_OPCODE;
		valid = false;
	}

	public final byte getOpCode() {
		assert(valid);
		return opCode;
	}
	
	public final String getOpCodeString() {
		assert(valid);
		switch (opCode) {
		case OP_REMOVE_SEED:
			return "REMOVE_SEED";
		case OP_REMOVE_SEED_ACK:
			return "REMOVE_SEED_ACK";
		case OP_ADD_SEED:
			return "ADD_SEED";
		case OP_ADD_SEED_ACK:
			return "ADD_SEED_ACK";
		case OP_QUERY_FILES:
			return "QUERY_FILES";
		case OP_FILE_LIST:
			return "FILE_LIST";
		case OP_GET_SEEDS:
			return "GET_SEEDS";
		case OP_SEED_LIST:
			return "SEED_LIST";
		default:
			return "INVALID_TYPE";
		}
	}
	
	/**
	 * @param opCode
	 */

	public final void setOpCode(byte opCode) {
		assert(!valid);
		_check_opcode(opCode);

		this.opCode = opCode;
	}

	/**
	 * 
	 * @param command User's command that determines type of request
	 * @param fileList List of file metadata to include in request
	 * @return Request message of appropriate type and data fields
	 */
	public static Message makeRequest(byte requestOpcode, int port, FileInfo[] fileList) {
		switch (requestOpcode) {
			case OP_QUERY_FILES:
			{
				return new MessageControl(requestOpcode);
			}
			case OP_GET_SEEDS:
			{
				assert(fileList.length == 1);
				InetSocketAddress[] seedList = new InetSocketAddress[0];
				return new MessageDataSeedInfo(requestOpcode, seedList, fileList[0].fileHash);
			}
			case OP_REMOVE_SEED:
			case OP_ADD_SEED:
			{
				return new MessageDataFileInfo(requestOpcode, port, fileList);
			}
			default:
		}
		assert(false); // Should never reach this point
		return null;		
	}

	/**
	 * Class method to parse a request message received by the tracker
	 * @param buf The byte array of the received packet 
	 * @return A message of the appropriate format representing this request 
	 */
	public static Message parseRequest(byte[] buf)
	{ 
		assert(buf.length > 0);
		byte reqOpcode = buf[0];
		switch(reqOpcode) {
			case OP_QUERY_FILES:
				return new MessageControl(reqOpcode); 
			case OP_REMOVE_SEED:
			case OP_ADD_SEED:
				return new MessageDataFileInfo(buf); 
			case OP_GET_SEEDS:
				return new MessageDataSeedInfo(buf);
			default:
				return null;
		}
	}

	/**
	 * Class method to parse a response message received by the client
	 * @param buf The byte array of the packet received from the tracker 
	 * @return A message of the appropriate format representing this response 
	 */
	public static Message parseResponse(byte[] buf)
	{ 
		assert(buf.length > 0);
		byte reqOpcode = buf[0];
		switch(reqOpcode) {
			case OP_REMOVE_SEED_ACK:
			case OP_ADD_SEED_ACK:
				return new MessageControl(reqOpcode); 
			case OP_FILE_LIST:
				return new MessageDataFileInfo(buf); 
			case OP_SEED_LIST:
				return new MessageDataSeedInfo(buf);
			default:
				return null;
		}
	}	
	

	public byte getResponseOpcode() {
		assert(valid);
		if (opCode == OP_REMOVE_SEED) {
			return OP_REMOVE_SEED_ACK;
		}
		else if (opCode == OP_ADD_SEED) {
			return OP_ADD_SEED_ACK;
		}
		else if (opCode == OP_QUERY_FILES) {
			return OP_FILE_LIST;
		}
		else if (opCode == OP_GET_SEEDS) {
			return OP_SEED_LIST;
		}
		else {
			throw new RuntimeException("Opcode " + opCode + " no tiene respuesta.");
		}
	}

	/* To check opcode validity */
	private static final Byte[] _valid_opcodes = {
			OP_REMOVE_SEED,
			OP_REMOVE_SEED_ACK,
			OP_ADD_SEED,
			OP_ADD_SEED_ACK,
			OP_QUERY_FILES,
			OP_FILE_LIST,
			OP_GET_SEEDS,
			OP_SEED_LIST,
			};
	private static final Set<Byte> valid_opcodes =
		Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_valid_opcodes)));

	// Protected to allow overriding in subclasses
	protected void _check_opcode(byte opcode)
	{
		if (!valid_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " no es válido.");
	}

}
