import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import GivenTools.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.nio.CharBuffer;


public class TrackerConnection {
  private TorrentInfo tInfo;
  private String peer_id = "DONDESTALABIBLIOTECA";
  private int portNum = 80;

  public TrackerConnection(TorrentInfo tInfo) {
    this.tInfo = tInfo;
  }

  @SuppressWarnings("unchecked")
  public Peer[] getPeerList() {
    byte[] data = this.sendGETRequest();
    if(data == null) return null; // error with get request
    // filter the peers from the becoded data
    try{
          Map<ByteBuffer, Object> map = (Map<ByteBuffer, Object>)Bencoder2.decode(data);
          ArrayList<Map<ByteBuffer, Object>> list = (ArrayList<Map<ByteBuffer, Object>>)map
            .get(str_to_bb("peers"));
          ArrayList<Peer> peers = new ArrayList<Peer>();
          //go through each peer from the list
          for(int i = 0; i < list.size(); ++i) {
            Map<ByteBuffer, Object> peer = (Map<ByteBuffer, Object>) list.get(i);
            ByteBuffer buff = (ByteBuffer) peer.get(str_to_bb("peer id"));
            String id = new String(buff.array());
            // check if peer starts with -RU
            if(!id.substring(0, 3).equals("-RU")) {
                continue;
            }
            buff = (ByteBuffer) peer.get(str_to_bb("ip"));
            String ip = new String(buff.array());
            int port = (Integer) peer.get(str_to_bb("port"));
            peers.add(new Peer(id, ip, port));
          }

          return peers.toArray(new Peer[peers.size()]);        
    } catch(BencodingException be) {
        System.err.println("Error decoding Tracker response!\n");
        System.err.println(be.toString());
        return null;
    }
  }

  public int disconnect() {
    URL trackerURL = getTrackerURL(tInfo, "stopped");
    
    try {
        HttpURLConnection con = (HttpURLConnection) trackerURL.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        con.disconnect();
        return responseCode;
    } catch(IOException io) {
        System.err.println("Failed to disconnect to tracker!\n");
        System.err.println(io);
        return -1;
    }
  }

  /*
   * Sends the tracker a get request and returns a byte array of the response
   */
  private byte[] sendGETRequest() {
    URL trackerURL = getTrackerURL(tInfo, "started");
    try {
      HttpURLConnection con = (HttpURLConnection) trackerURL.openConnection();
      con.setRequestMethod("GET");
      int responseCode = con.getResponseCode();
      System.out.println("\nSending 'GET' request to URL : " + trackerURL.toString());
      System.out.println("\nResponse Code : " + responseCode);
      //read the input stream into a bytearraystream and into a byte array
      InputStream is = con.getInputStream();
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      int nRead;
      byte[] data = new byte[512];

      while((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
      }

      buffer.flush();
      data = buffer.toByteArray();
      // close the stream and connection
      is.close();
      con.disconnect();
      return data;  
    } catch(IOException ie) {
      System.err.println("Error occured while opening connection to tracker!");
      System.err.println(ie.toString());
      return null;
    }
  }

  /*
    Helper method to convert a ByteBuffer into a Hex String
    Needed to put hashes in url.
    @param buffer ByteBuffer
    @return hexStr is null if buffer is null else is unicode hex string
  */
  private String byteBuffertoHexStr(ByteBuffer buffer) {
    byte[] bytes; // Get the bytes array from the buffer
    if(buffer.hasArray()) {
        bytes = buffer.array();
    } else {
        bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
    }

    // Build an unicode hexadecimalstring with each byte
    StringBuilder hexStr = new StringBuilder(bytes.length);
    int v; String hex;
    for ( int j = 0; j < bytes.length; j++ ) {
        v = bytes[j] & 0xFF; // get the byte
        //Get the last two bytes of the hex number
        hex = Integer.toHexString(0x100 | v).substring(1);
        hexStr.append("%"+hex);
    }
    return hexStr.toString();
  }

  /*
    Builds the tracker url from the torrent info
    @param tInfo TorrentInfo containing metainfo needed to create the tracker url
    @return trackerURLStr url to be used for the GET request
  */
  private URL getTrackerURL(TorrentInfo tInfo, String event) {
    if(tInfo == null) return null;

    String trackerURLStr = null;
    URL trackerURL = null;
    try {
      // build the URL string
      trackerURLStr = tInfo.announce_url.toString() + "?info_hash=" +
        byteBuffertoHexStr(tInfo.info_hash)  +
        "&peer_id=" + URLEncoder.encode(peer_id, "UTF-8") +
        "&port=" + URLEncoder.encode(""+portNum, "UTF-8") +
        "&uploaded=0&downloaded=0&left=" +
        URLEncoder.encode(""+tInfo.file_length, "UTF-8") +
        "&event="+event;

      // build the url object
      trackerURL = new URL(trackerURLStr);
    } catch (UnsupportedEncodingException uee) {
      System.err.println("Need support for UTF-8\n" + uee.toString());
    } catch (MalformedURLException mue) {
      System.err.println("Contacting tracker failed!\n" + mue.toString());
    }

    return trackerURL;
  }
  
  private static Charset charset = Charset.forName("UTF-8");
  private static CharsetEncoder encoder = charset.newEncoder();

  private static ByteBuffer str_to_bb(String msg) {
    try {
        return encoder.encode(CharBuffer.wrap(msg));
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
  }

}
