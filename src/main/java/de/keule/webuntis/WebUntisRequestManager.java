package de.keule.webuntis;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.json.XML;

public class WebUntisRequestManager {
  private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36 OPR/83.0.4254.46";
  private static final String DEFAULT_ENDPOINT = "WebUntis/jsonrpc.do";
  private static boolean printRequests = false;

  public static WebUntisResponse requestPOST(WebUntisRequestMethod method, WebUntisSessionInfo session,
                                             String endPoint, String school, String params) throws IOException {
    return requestPOST(method.NAME, session, endPoint, school, params);
  }

  public static WebUntisResponse requestPOST(String method, WebUntisSessionInfo session, String endPoint,
                                             String school, String params) throws IOException {
    final URL url;
    try {
      url = new URI(session.getServer() + getEndPoint(endPoint) + "?school=" + school).toURL();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    if (params == null || params.isEmpty())
      params = "{}";
    final String request = "{\"id\":\"" + session.getRequestId() + "\",\"method\":\"" + method + "\",\"params\":"
            + params + ",\"jsonrpc\":\"2.0\"}";

    if (printRequests) {
      System.out.println("Request[POST]: " + url.toExternalForm());
      System.out.println("Payload: " + request);
    }

    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    con.setDoOutput(true);
    con.setRequestMethod("POST");
    con.setInstanceFollowRedirects(true);
    con.setRequestProperty("User-Agent", userAgent);
    con.setRequestProperty("Content-Type", "application/json");
    if (session.isActive() && method != "getAuthToken")
      con.setRequestProperty("Cookie",
              "JSESSIONID=" + session.getSessionId() + "; schoolname=" + session.getSchoolname());
    if (method == "getAuthToken")
      con.setRequestProperty("Cookie", "schoolname=" + session.getSchoolname() + "; Tenant-Id=5403700");
    // Write request body
    OutputStream outputStream = con.getOutputStream();
    outputStream.write(request.getBytes());

    // Read response
    BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    while ((line = input.readLine()) != null) {
      stringBuilder.append(line);
    }

    // Get sessionId and schoolName
    if (!session.isActive()) {
      for (String key : con.getHeaderFields().keySet()) {
        for (String s : con.getHeaderFields().get(key)) {
          if (!s.contains("JSESSIONID") && !s.contains("schoolname"))
            continue;

          String[] split = s.split(";");
          for (String spl : split) {
            if (spl.contains("JSESSIONID")) {
              session.setSessionId(spl.replace("JSESSIONID=", ""));
            }
            if (spl.contains("schoolname")) {
              session.setSchoolName(spl.replace("schoolname=", "").replaceAll("\"", ""));
            }
          }
        }
      }
    }

    // Close
    outputStream.close();
    input.close();
    con.disconnect();

    return new WebUntisResponse(new JSONObject(stringBuilder.toString()));
  }

  /**
   * @param wurl     The URL of the WebUntis server
   * @param endPoint The endpoint to send the request to
   * @param schoolName The name of the school
   * @param tenantId The tenant ID
   * @return Returns the Image as a BufferedImage object
   * @throws Exception
   */
  public static BufferedImage requestGETImage(String wurl, String endPoint, String schoolName, int tenantId) throws Exception {
    final URL url;
    try {
      url = new URI(wurl + endPoint).toURL();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    if (printRequests) {
      System.out.println("Request[GET]: " + url.toExternalForm());
    }

    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    // Set request method
    con.setRequestMethod("GET");

    // Set headers
    con.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
    con.setRequestProperty("Accept-Encoding", "gzip, deflate, br, zstd");
    con.setRequestProperty("Cookie", "schoolname=\"_" + Base64.getEncoder().encodeToString(schoolName.getBytes()) +
            "\"; Tenant-Id=\"" + tenantId + "\"; JSESSIONID=06FE4158D276FEEF5B9F04E33C81073B;");
    con.setRequestProperty("User-Agent", userAgent);
    con.setRequestProperty("Referer", "https://arche.webuntis.com/WebUntis/?school=" + schoolName);

    // Get response code
    int responseCode = con.getResponseCode();

    // Handle response
    if (responseCode == HttpURLConnection.HTTP_OK) { // success
      // Get input stream and read it as a BufferedImage
      InputStream inputStream = con.getInputStream();
      BufferedImage image = ImageIO.read(inputStream);  // Convert the input stream into a BufferedImage
      inputStream.close();  // Close the input stream

      return image;  // Return the BufferedImage object
    } else {
      System.out.println("GET request failed.");
      return null;  // Return null in case of failure
    }
  }


  public static WebUntisResponse requestGET(WebUntisSessionInfo session, String endPoint) throws IOException {
    final URL url;
    try {
      url = new URI(session.getServer() + endPoint).toURL();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    if (printRequests) {
      System.out.println("Request[GET]: " + url.toExternalForm());
    }

    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setInstanceFollowRedirects(true);
    con.setRequestProperty("User-Agent", userAgent);
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Cookie",
            "JSESSIONID=" + session.getSessionId() + "; schoolname=" + session.getSchoolname());
    // Read response
    BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    while ((line = input.readLine()) != null) {
      stringBuilder.append(line);
    }

    // Close
    input.close();
    con.disconnect();

    // Check Content-Type and convert XML to JSON if necessary
    String contentType = con.getHeaderField("Content-Type");
    String responseString = stringBuilder.toString();
    if (contentType != null && contentType.contains("application/xml")) {
      JSONObject jsonResponse = XML.toJSONObject(responseString);
      // Remove the JsonApiDataDocument wrapper
      if (jsonResponse.has("JsonApiDataDocument")) {
        jsonResponse = jsonResponse.getJSONObject("JsonApiDataDocument");
      }
      responseString = jsonResponse.toString();
    }

    return new WebUntisResponse(new JSONObject(responseString));
  }

  private static String getEndPoint(String endPoint) {
    if (endPoint == null || endPoint.isEmpty())
      return DEFAULT_ENDPOINT;
    if (endPoint.charAt(0) == '/')
      return endPoint.substring(1);
    return endPoint;
  }

  public static void setUserAgent(String newUserAgent) {
    userAgent = newUserAgent;
  }

  public static String getUserAgent() {
    return userAgent;
  }

  public static boolean printRequests() {
    return printRequests;
  }

  public static void setPrintRequest(boolean value) {
    printRequests = value;
  }
}