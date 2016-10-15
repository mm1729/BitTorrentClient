import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {

  public static ByteArrayOutputStream inputStreamToByteStream(InputStream is)
    throws IOException{
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return buffer;
  }

  public static byte[] inputStreamToByteArr(InputStream is) throws IOException{
    return inputStreamToByteStream(is).toByteArray();
  }
}
