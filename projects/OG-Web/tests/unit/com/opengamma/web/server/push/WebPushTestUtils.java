/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class WebPushTestUtils {

  private static final int PORT = 8084;
  private static final String URL_BASE = "http://localhost:" + PORT;

  private WebPushTestUtils() {
  }

  /* package */ static URL url(String path) throws MalformedURLException {
    return new URL(URL_BASE + path);
  }

  /* package */ public static String readFromPath(String path) throws IOException {
    return readFromPath(path, null);
  }
  /* package */ public static String readFromPath(String path, String clientId) throws IOException {
    return readFromPath(path, clientId, "GET");
  }

  /* package */
  public static String handshake() throws IOException {
    String json = readFromPath("/handshake");
    try {
      return new JSONObject(json).getString("clientId");
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create JSON from handshake response: " + json, e);
    }
  }

  /* package */ static String readFromPath(String path, String clientId, String requestMethod) throws IOException {
    String fullPath;
    if (clientId != null) {
      fullPath = path + "?clientId=" + clientId;
    } else {
      fullPath = path;
    }
    BufferedReader reader = null;
    HttpURLConnection connection = null;
    StringBuilder builder;
    try {
      char[] chars = new char[512];
      builder = new StringBuilder();
      URL url = url(fullPath);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(requestMethod);
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      int bytesRead;
      while ((bytesRead = reader.read(chars)) != -1) {
        builder.append(chars, 0, bytesRead);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
    return builder.toString();
  }

  public static HttpURLConnection connectToPath(String path) throws IOException {
    URL url = url(path);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    return connection;
  }

  public static String readAndClose(HttpURLConnection connection) throws IOException {
    BufferedReader reader = null;
    StringBuilder builder;
    try {
      char[] chars = new char[512];
      builder = new StringBuilder();
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      int bytesRead;
      while ((bytesRead = reader.read(chars)) != -1) {
        builder.append(chars, 0, bytesRead);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
    return builder.toString();
  }

  /**
   * Creates and starts a Jetty server using {@code web-push/WEB-INF/web.xml} and configured using Spring
   * @return The server and the Spring context
   * @param springXml The location of the Spring XML config file
   */
  public static Pair<Server, WebApplicationContext> createJettyServer(String springXml) throws Exception {
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(PORT);
    WebAppContext context = new WebAppContext();
    context.setContextPath("/");
    context.setResourceBase("build/classes");
    context.setDescriptor("web-push/WEB-INF/web.xml");
    Map<String, String> params = new HashMap<String, String>();
    params.put("contextConfigLocation", springXml);
    context.setInitParams(params);
    context.addEventListener(new ContextLoaderListener());
    Server server = new Server();
    server.addConnector(connector);
    server.setHandler(context);
    server.start();
    WebApplicationContext springContext = (WebApplicationContext) context.getServletContext().getAttribute(
            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    return new ObjectsPair<Server, WebApplicationContext>(server, springContext);
  }

  /**
   * Asserts that {@code json} represents a JSON object with a field called {@code updates} whose value is an array
   * of the expected values.
   * @param json {@code {updates: [url1, url2, ...]}}
   * @param urls URLs that must be present in the JSON
   */
  static void checkJsonResults(String json, String... urls) throws JSONException {
    List<String> expectedList = Arrays.asList(urls);
    JSONArray results = new JSONObject(json).getJSONArray(LongPollingUpdateListener.UPDATES);
    assertEquals("Wrong number of results.  expected: " + expectedList + ", actual: " + results, expectedList.size(), results.length());
    for (int i = 0; i < results.length(); i++) {
      String result = results.getString(i);
      assertTrue("Unexpected result: " + result, expectedList.contains(result));
    }
  }

  /**
   * @return The URL of the viewport relative to the root
   */
  public static String createViewport(String clientId, String viewportDefJson) throws IOException, JSONException {
    String viewportJson;
    BufferedReader reader = null;
    BufferedWriter writer = null;
    try {
      URL url = new URL("http://localhost:" + PORT + "/jax/viewports?clientId=" + clientId);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("CONTENT-TYPE", MediaType.APPLICATION_JSON);
      connection.connect();
      writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
      writer.write(viewportDefJson);
      writer.flush();
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      viewportJson = reader.readLine();
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
    return new JSONObject(viewportJson).getString("viewportUrl");
  }
}
