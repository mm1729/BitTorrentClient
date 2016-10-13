import java.io.*;
import java.net.Socket;
import GivenTools.*;
import java.net.UnknownHostException;
import java.util.Arrays;

public class PeerDownload{
	private String clientId;
	private TorrentInfo tInfo;
	private Peer[] peerList;
	private Socket peerConnection;
	public String protoName = "BitTorrent protocol";
	public int protoNameLen = 19;
	public PeerDownload(TorrentInfo tInfo,String clientId, Peer[] peers){
		this.peerList = peers;
		this.clientId =  clientId;
		this.tInfo = tInfo;
	}	

	
	public void download(){
		for( Peer peerEntry: peerList){

			try{
			peerConnection = new Socket(peerEntry.getIp(),peerEntry.getPort());
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(peerConnection.getOutputStream()));

			DataInputStream  in  = new DataInputStream(new BufferedInputStream(peerConnection.getInputStream()));
			ByteArrayOutputStream msg = new ByteArrayOutputStream();
			msg.write(protoNameLen);
			msg.write(protoName.getBytes());
			for(int i =0; i<8; i++){
				msg.write(0);
			}
			msg.write(clientId.getBytes());
			msg.write(tInfo.info_hash.array());
			
			out.write(msg.toByteArray());
								/*
			in.readFully(buf);
			for(byte b: buf){
				char c = (char)b;
				System.out.print(c);
			}*/
		
			int pstrlen = (int)in.readByte();
			if(pstrlen != protoNameLen){
				//throw error
			}
			byte[] byte_pstr = new byte[pstrlen];
			
			in.readFully(byte_pstr,0,pstrlen);
				
			String pstr = new String(byte_pstr);
		    	if(pstr.equals(protoName) == false){
				//throw error
			}
			System.out.println(pstr);
			in.skipBytes(8);
			byte[] byte_hash = new byte[20];
			
			in.readFully(byte_hash,0,20);
		  	if(!Arrays.equals(byte_hash,tInfo.info_hash.array())){
				System.out.println("ERRRORRORO!");
			}
			byte[] byte_peer = new byte[20];
			in.readFully(byte_peer,0,20);
			String peer_id = new String(byte_peer);
			if(!peer_id.equals(peerEntry.getId())){
				System.out.println("EERSDASDSDA!!!!");
			}	
			System.out.println(peer_id);		
		    }

		    catch(UnknownHostException e){
		    	    e.printStackTrace();
		    }

		    catch(IOException e){
			    e.printStackTrace();
		    }
		}
		

	}





}
