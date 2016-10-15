import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message {
	/*
	 * All message types arranged by ids
	 */
	public enum MessageType {
		KEEPALIVE, CHOKE, UNCHOKE, INTERESTED, NOTINTERESTED, HAVE, 
		BITFIELD, REQUEST, PIECE, CANCEL
	}
	private MessageType type;
	private int len;
	private Payload payLoad;

	public Message(byte[] msg) {
		switch(this.type = getType(msg)) {
			case KEEPALIVE:
				break;
			case CHOKE:
			case UNCHOKE:
			case INTERESTED:
			case NOTINTERESTED:
				this.len = 1;
				break;
			case HAVE:
				this.len = 5;
				this.payLoad = new Payload(this.type, new byte[]{msg[4]});
				break;
			case BITFIELD:
			case REQUEST:
			case PIECE:
			case CANCEL:
				byte[] len = Arrays.copyOfRange(msg, 0, 4);
				byte[] rest = Arrays.copyOfRange(msg, 5, msg.length);
				this.len = ByteBuffer.wrap(len).getInt();
				this.payLoad = new Payload(this.type, rest);
				break;
			default:
				throw new IllegalArgumentException("Unknown Message Type\n");
		}

	}

	private MessageType getType(byte[] msg) {
		if(msg.length <= 4) return MessageType.KEEPALIVE;
		return MessageType.values()[msg[4] + 1]; // add one for enum
	}
	
	/*
	 * Payload class parses and keeps track of all pieces of a payload
	 */
	class Payload {
		private int pieceIndex;
		private byte[] bitField;
		private int begin;
		private int length;
		private byte[] block;

		public Payload(MessageType type, byte[] payLoad) {
			/*
			 * Create appropriate fields based on message type
			 */
			switch (type) {
				case HAVE:
					this.pieceIndex = 
						ByteBuffer.wrap(payLoad).getInt();
					break;
				case BITFIELD:
					this.bitField = payLoad; break;
				case REQUEST: case CANCEL:
					byte[] indexB = Arrays.copyOfRange(payLoad, 0, 4);
					byte[] beginB = Arrays.copyOfRange(payLoad, 4, 9);
					byte[] lengthB = Arrays.copyOfRange(payLoad, 9, 12);
					this.pieceIndex = 
						ByteBuffer.wrap(indexB).getInt();
					this.begin = 
						ByteBuffer.wrap(beginB).getInt();
					this.length =
						ByteBuffer.wrap(lengthB).getInt();
					break;
				case PIECE:
					indexB = Arrays.copyOfRange(payLoad, 0, 4);
					beginB = Arrays.copyOfRange(payLoad, 4, 9);
					this.pieceIndex = 
						ByteBuffer.wrap(indexB).getInt();
					this.begin = 
						ByteBuffer.wrap(beginB).getInt();
					this.block = Arrays.copyOfRange(payLoad, 9, payLoad.length);
					break;
				default:
					throw new IllegalArgumentException("Type "+type.name()+
							" has no payload");
						
			}
		}
	}
}


