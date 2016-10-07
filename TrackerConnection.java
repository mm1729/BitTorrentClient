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

  public TrackerConnection(TorrentInfo tInfo) {
    this.tInfo = tInfo;
  }

  public void getPeerList() {

  }

  private void sendGETRequest() {
    URL trackerURL = getTrackerURL(tInfo);
    try {
      HttpURLConnection con = (HttpURLConnection) trackerURL.openConnection();
      con.setRequestMethod("GET");
      int responseCode = con.getResponseCode();
      System.out.println("\nSending 'GET' request to URL : " + trackerURL.toString());
      System.out.println("Response Code : " + responseCode);

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
  private static String byteBuffertoHexStr(ByteBuffer buffer) {
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
  private static URL getTrackerURL(TorrentInfo tInfo) {
    if(tInfo == null) return null;

    String trackerURLStr = null;
    URL trackerURL = null;
    try {
      // build the URL string
      trackerURLStr = tInfo.announce_url.toString() + "?info_hash=" +
        URLEncoder.encode(byteBuffertoHexStr(tInfo.info_hash), "UTF-8") +
        "&peer_id=DONDESTALABIBLIOTECA" +
        "&port=80" + "&left=" + tInfo.file_length;

      // build the url object
      trackerURL = new URL(trackerURLStr);
    } catch (UnsupportedEncodingException uee) {
      System.err.println("Need support for UTF-8" + uee.toString());
    } catch (MalformedURLException mue) {
      System.err.println("Contacting tracker failed!\n" + mue.toString());
    }

    return trackerURL;
  }
}
