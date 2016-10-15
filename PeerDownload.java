import java.io.*;
import java.net.Socket;
import GivenTools.*;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.lang.*;

public class PeerDownload{
	private String clientId;
	private TorrentInfo tInfo;
	private Peer[] peerList;
	private Socket peerConnection;
	private final String protoName = "BitTorrent protocol";
	private final int protoNameLen = protoName.length();

	public PeerDownload(TorrentInfo tInfo,String clientId, Peer[] peers){
		this.peerList = peers;
		this.clientId =  clientId;
		this.tInfo = tInfo;
	}

	/*
	Constructs the handshake message
	*/
	private ByteArrayOutputStream getHandshakeMsg() throws IOException{
		ByteArrayOutputStream msg = new ByteArrayOutputStream();
		msg.write(protoNameLen);
		msg.write(protoName.getBytes());
		for(int i = 0; i<8; i++){
			msg.write(0);
		}
		msg.write(tInfo.info_hash.array());
		msg.write(clientId.getBytes());

		return msg;
	}

	private boolean receiveHandshake(DataInputStream in, Peer peerEntry)
		throws IOException{
		byte[] handshake = Utils.inputStreamToByteArr(in);
		// check the handshake - pstrlen
		int pstrLen = (int) handshake[0];
		if(pstrLen != protoNameLen){
			return false;
		}

		String pstr = new String(Arrays.copyOfRange(handshake, 1, pstrLen+1));
		if(pstr.equals(protoName) == false){
			return false;
		}

		String bytePeer = new String(Arrays.copyOfRange(handshake,
			pstrLen+1+8, handshake.length));

		if(peerEntry.getId().equals(peerEntry.getId()) == false){
			return false;
		}
		return true;
	}

	public void download(){
		for( Peer peerEntry : peerList){

			try{
			peerConnection = new Socket(peerEntry.getIp(),peerEntry.getPort());
			DataOutputStream out = new DataOutputStream(
				new BufferedOutputStream(peerConnection.getOutputStream()));

			DataInputStream  in  = new DataInputStream(
				new BufferedInputStream(peerConnection.getInputStream()));


			// Send handshake to peer
			try {
				ByteArrayOutputStream msg = getHandshakeMsg();
				out.write(msg.toByteArray());
				out.flush();
			} catch(IOException ie) {
				System.err.printf("Failed to send hanshake to peer %s",
					peerEntry.getId());
				return;
			}

			int numPieces = tInfo.file_length/tInfo.piece_length +
				((tInfo.file_length % tInfo.piece_length == 0) ? 0 : 1);
			System.out.printf("\nThe file length is %d. The piece length is %d. There are %d pieces to download.\n",
				tInfo.file_length, tInfo.piece_length, numPieces);

			//Read handshake from peer
			try {
				if(receiveHandshake(in, peerEntry) != true) {
					System.err.printf("Did not receive proper handshake from peer %s",
						peerEntry.getId());
					return;
				}
			} catch(IOException ie) {
				System.err.printf("Failed to read handshake from peer %s",
					peerEntry.getId());
				return;
			}


			/*byte[] interested = {1,0,0,0,2};
			out.write(interested, 0, interested.length);
			out.flush();*/

			/*[] keepAlive = {0,0,0,0};
			out.write(keepAlive, 0, keepAlive.length);
			out.flush();*/



		    }

		    catch(UnknownHostException e){
		    	    e.printStackTrace();
		    }

		    catch(IOException e){
			    e.printStackTrace();
		    }
		}


	}

	private byte[] getBitfieldMsg(int numPieces) {
		return null;
	}




}
