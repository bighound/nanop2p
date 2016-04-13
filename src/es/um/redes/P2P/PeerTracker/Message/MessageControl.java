/**
 *
 */
package es.um.redes.P2P.PeerTracker.Message;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rtitos
 * 
 * Peer-tracker protocol control message 
 * 
    1b    
 +-------+
 |opcode |
 +-------+
             
 */

public class MessageControl extends Message 
{
	/**
	 * Control message opcodes 
	 */
	protected static final Byte[] _control_opcodes = {
			OP_QUERY_FILES,			
			OP_REMOVE_SEED_ACK,
			OP_ADD_SEED_ACK
			};

	/** 
	 * Constructor
	 * @param opcode opcode indicating the type of message
	 */
	public MessageControl(byte opcode) {
		setOpCode(opcode);
		valid = true;
	}
	
	/**
	 * Constructor used by tracker when creating message response after receiving
	 * @param buf The byte array containing the packet buffer
	 */
	public boolean fromByteArray(byte[] array) {
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setOpCode(buf.get());
			valid = true;		
		} catch (RuntimeException e) {
			e.printStackTrace();
			assert(valid == false);
		}
		return valid;
	}

	/**
	 * Creates a byte array ready for sending a datagram packet, from a valid control message  
	 */
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(1);

		// Opcode
		buf.put((byte)this.getOpCode());
		return buf.array();
	}

	public String toString() {
		assert(valid);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("Tipo:"+this.getOpCodeString());
		return strBuf.toString();
	}

	/* For checking opcode validity */
	private static final Set<Byte> control_opcodes =
			Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_control_opcodes)));

	protected void _check_opcode(byte opcode)
	{
		if (!control_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " no es de tipo control.");
	}

}
