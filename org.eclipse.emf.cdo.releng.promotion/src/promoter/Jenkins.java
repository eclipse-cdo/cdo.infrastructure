package promoter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

/**
 *
 * @author Eike Stepper
 */
public final class Jenkins
{
  private static final String USERNAME = System.getProperty("jenkinsUsername");

  private static final String PASSWORD = System.getProperty("jenkinsPassword");

  private static final String ENCODED = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());

  public static InputStream openInputStream(String url) throws IOException
  {
    URL urlObj = URI.create(url).toURL();

    HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
    conn.setRequestProperty("Authorization", "Basic " + ENCODED);
    conn.setRequestMethod("GET");
    conn.setInstanceFollowRedirects(true);
    conn.connect();

    int code = conn.getResponseCode();
    if (code != HttpURLConnection.HTTP_OK)
    {
      throw new IOException("HTTP error: " + code + " " + conn.getResponseMessage());
    }

    return conn.getInputStream();
  }
}
