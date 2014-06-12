/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

public class WebPushTestUtils {

  /**
   * The port to use, or {@code 0} if none has been agreed yet.
   */
  private int _port;

  private String _urlBase;

  public WebPushTestUtils() {
  }

  public WebPushTestUtils(final int port) {
    setPort(port);
  }

  private void setPort(final int port) {
    _port = port;
    _urlBase = "http://localhost:" + port;
  }

  /* package */ URL url(final String path) throws MalformedURLException {
    return new URL(_urlBase + path);
  }

  /* package */ public String readFromPath(final String path) throws IOException {
    return readFromPath(path, null);
  }
  /* package */ public String readFromPath(final String path, final String clientId) throws IOException {
    return readFromPath(path, clientId, "GET");
  }

  /* package */
  public String handshake() throws IOException {
    final String json = readFromPath("/handshake");
    try {
      return new JSONObject(json).getString("clientId");
    } catch (final JSONException e) {
      throw new IllegalArgumentException("Failed to create JSON from handshake response: " + json, e);
    }
  }

  /* package */ String readFromPath(final String path, final String clientId, final String requestMethod) throws IOException {
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
      final char[] chars = new char[512];
      builder = new StringBuilder();
      final URL url = url(fullPath);
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

  public HttpURLConnection connectToPath(final String path) throws IOException {
    final URL url = url(path);
    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    return connection;
  }

  public static String readAndClose(final HttpURLConnection connection) throws IOException {
    BufferedReader reader = null;
    StringBuilder builder;
    try {
      final char[] chars = new char[512];
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
  public Pair<Server, WebApplicationContext> createJettyServer(final String springXml) throws Exception {
    final WebAppContext context = new WebAppContext();
    context.setContextPath("/");
    context.setResourceBase("build/classes");
    context.setDescriptor("web-push/WEB-INF/web.xml");
    context.setInitParameter("contextConfigLocation", springXml);
    context.addEventListener(new ContextLoaderListener());
    final Server server = new Server();
    server.setHandler(context);
    final SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(_port);
    server.addConnector(connector);
    server.start();
    if (_port == 0) {
      setPort(connector.getLocalPort());
    }
    final Context servletContext = context.getServletContext();
    final WebApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    final Map<String, ConnectionManager> cmMap = springContext.getBeansOfType(ConnectionManager.class);
    if (cmMap.size() == 1) {
      WebPushServletContextUtils.setConnectionManager(servletContext, cmMap.values().iterator().next());
    }
    final Map<String, LongPollingConnectionManager> lpcmMap = springContext.getBeansOfType(LongPollingConnectionManager.class);
    if (lpcmMap.size() == 1) {
      WebPushServletContextUtils.setLongPollingConnectionManager(servletContext, lpcmMap.values().iterator().next());
    }
    return Pairs.of(server, springContext);
  }

  /**
   * Asserts that {@code json} represents a JSON object with a field called {@code updates} whose value is an array
   * of the expected values.
   * @param json {@code {updates: [url1, url2, ...]}}
   * @param urls URLs that must be present in the JSON
   */
  static void checkJsonResults(final String json, final String... urls) throws JSONException {
    final List<String> expectedList = Arrays.asList(urls);
    final JSONArray results = new JSONObject(json).getJSONArray(LongPollingUpdateListener.UPDATES);
    assertEquals("Wrong number of results.  expected: " + expectedList + ", actual: " + results, expectedList.size(), results.length());
    for (int i = 0; i < results.length(); i++) {
      final String result = results.getString(i);
      assertTrue("Unexpected result: " + result, expectedList.contains(result));
    }
  }

  /**
   * @return The URL of the viewport relative to the root
   */
  public String createViewport(final String clientId, final String viewportDefJson) throws IOException, JSONException {
    String viewportJson;
    BufferedReader reader = null;
    BufferedWriter writer = null;
    try {
      final URL url = new URL("http://localhost:" + _port + "/jax/viewports?clientId=" + clientId);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
