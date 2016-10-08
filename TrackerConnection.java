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

public class TrackerConnection {
  private TorrentInfo tInfo;
  private String peer_id = "DONDESTALABIBLIOTECA";
  private int portNum = 80;

  public TrackerConnection(TorrentInfo tInfo) {
    this.tInfo = tInfo;
  }

  public void getPeerList() {
    this.sendGETRequest();
  }

  private void sendGETRequest() {
    URL trackerURL = getTrackerURL(tInfo);
    try {
      HttpURLConnection con = (HttpURLConnection) trackerURL.openConnection();
      con.setRequestMethod("GET");
      int responseCode = con.getResponseCode();
      System.out.println("\nSending 'GET' request to URL : " + trackerURL.toString());
      System.out.println("\nResponse Code : " + responseCode);

      BufferedReader in = new BufferedReader(
          new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      //print result
      System.out.println(response.toString());
    } catch(IOException ie) {
      System.err.println("Error occured while opening connection to tracker!");
      System.err.println(ie.toString());
      return;
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
    int v;
    for ( int j = 0; j < bytes.length; j++ ) {
        v = bytes[j] & 0xFF; // get the byte
        hexStr.append(Character.toString((char)v)); // append hex string
    }
    return hexStr.toString();
  }

  /*
    Builds the tracker url from the torrent info
    @param tInfo TorrentInfo containing metainfo needed to create the tracker url
    @return trackerURLStr url to be used for the GET request
  */
  private URL getTrackerURL(TorrentInfo tInfo) {
    if(tInfo == null) return null;

    String trackerURLStr = null;
    URL trackerURL = null;
    try {
      // build the URL string
      trackerURLStr = tInfo.announce_url.toString() + "?info_hash=" +
        URLEncoder.encode(byteBuffertoHexStr(tInfo.info_hash), "UTF-8") +
        "&peer_id=" + URLEncoder.encode(peer_id, "UTF-8") +
        "&port=" + URLEncoder.encode(""+portNum, "UTF-8") +
        "&uploaded=0&downloaded=0&left=" +
        URLEncoder.encode(""+tInfo.file_length, "UTF-8");

      // build the url object
      trackerURL = new URL(trackerURLStr);
    } catch (UnsupportedEncodingException uee) {
      System.err.println("Need support for UTF-8\n" + uee.toString());
    } catch (MalformedURLException mue) {
      System.err.println("Contacting tracker failed!\n" + mue.toString());
    }

    return trackerURL;
  }
}
