import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import GivenTools.*;


public class RUBTClient {

  /*
    IO helper method - reads torrentfileName to return a byte array
    @param torrentFileName string containing file name of the torrent file
    @return torrentFileBytes byte array containing file
  */
  private static byte[] getByteArray(String torrentFileName) {
    File torrentFile = new File(torrentFileName);

    // Convert torrentFile into a byte array
    byte[] torrentFileBytes = new byte[(int)torrentFile.length()];
    DataInputStream dis;
    try{
      dis = new DataInputStream(new FileInputStream(torrentFile));
      dis.readFully(torrentFileBytes);
      dis.close(); // no need of dis -- close it
      return torrentFileBytes;
    } catch(FileNotFoundException fne) {
      // Could not find file while trying to open a input stream of it
      System.err.printf("File %s does not exist\n", torrentFileName);
      return null;
    } catch(IOException e) {
      // Failed to readFully into the bytes array
      System.err.println("Could not read file into byte array\n");
      return null;
    }
  }


  public static void main(String[] args){

    if(args.length != 1) { // Check input args is correct usage
      System.err.println("USAGE:java RUBTClient <torrent_file>");
      return;
    }

    String torrentFileName = args[0];
    byte[] torrentFileBytes = getByteArray(torrentFileName);
    if(torrentFileBytes == null) return; // some error occured

    try {
      TorrentInfo tInfo = new TorrentInfo(torrentFileBytes);
      System.out.printf("Trying to torrent %s\n", tInfo.file_name);

      TrackerConnection conn = new TrackerConnection(tInfo);
      Peer[] peerList = conn.getPeerList();
      for(Peer p : peerList)
         System.out.print(p);
      
      int disconnectVal = conn.disconnect();
      System.out.println("Disconnected with Response Code " + disconnectVal);
      if(disconnectVal == -1) {
        //failed to disconnect
      }
    } catch(BencodingException be) {
      System.err.println(be);
      return;
    }



  }
}
