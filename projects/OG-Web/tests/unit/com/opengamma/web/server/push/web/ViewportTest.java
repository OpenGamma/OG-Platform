/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.util.tuple.Pair;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.opengamma.web.server.push.web.WebPushTestUtils.handshake;
import static com.opengamma.web.server.push.web.WebPushTestUtils.readFromPath;
import static org.testng.Assert.assertEquals;

/**
 *
 */
public class ViewportTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewportTest.class);

  private Server _server;

  @BeforeClass
  void createJettyServer() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/viewport-test.xml");
    _server = serverAndContext.getFirst();
    //WebApplicationContext context = serverAndContext.getSecond();
  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }

  @Test
  public void viewportData() throws Exception {
    String clientId = handshake();
    String viewportDefJson = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"rows\": [[0, 12345678], [1, 12345679], [2, 12345680]], " +
        "\"dependencyGraphCells\": [[1, 2]]" +
        "}" +
        "}";
    String viewportUrl = createViewport(clientId, viewportDefJson);
    if (StringUtils.isEmpty(viewportUrl)) {
      Assert.fail("no URL returned for viewport");
    }
    String latestResult = readFromPath(viewportUrl + "/data");
    JSONObject jsonResults = new JSONObject(latestResult);

    JSONArray row0 = jsonResults.getJSONArray("0");
    assertEquals(3, row0.length());
    assertEquals(1, row0.getInt(0));
    assertEquals(2, row0.getInt(1));
    assertEquals(3, row0.getInt(2));

    JSONArray row1 = jsonResults.getJSONArray("1");
    assertEquals(3, row1.length());
    assertEquals(2, row1.getInt(0));
    assertEquals(4, row1.getInt(1));
    assertEquals(6, row1.getInt(2));

    JSONArray row2 = jsonResults.getJSONArray("2");
    assertEquals(3, row2.length());
    assertEquals(3, row2.getInt(0));
    assertEquals(6, row2.getInt(1));
    assertEquals(9, row2.getInt(2));
  }

  @Test
  public void twoViewportsData() throws Exception {
    String clientId = handshake();
    String viewportDefJson = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"rows\": [[0, 12345678], [1, 12345679], [2, 12345680]], " +
        "\"dependencyGraphCells\": [[1, 2]]" +
        "}" +
        "}";
    String viewportUrl = createViewport(clientId, viewportDefJson);
    if (StringUtils.isEmpty(viewportUrl)) {
      Assert.fail("no URL returned for 1st viewport");
    }
    String latestResult = readFromPath(viewportUrl + "/data");
    JSONObject jsonResults = new JSONObject(latestResult);
    assertEquals(3, jsonResults.length());

    JSONArray row0 = jsonResults.getJSONArray("0");
    assertEquals(3, row0.length());
    assertEquals(1, row0.getInt(0));
    assertEquals(2, row0.getInt(1));
    assertEquals(3, row0.getInt(2));

    JSONArray row1 = jsonResults.getJSONArray("1");
    assertEquals(3, row1.length());
    assertEquals(2, row1.getInt(0));
    assertEquals(4, row1.getInt(1));
    assertEquals(6, row1.getInt(2));

    JSONArray row2 = jsonResults.getJSONArray("2");
    assertEquals(3, row2.length());
    assertEquals(3, row2.getInt(0));
    assertEquals(6, row2.getInt(1));
    assertEquals(9, row2.getInt(2));

    // different viewport for the same client ID -----

    viewportDefJson = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"rows\": [[2, 12345688], [3, 12345689]], " +
        "\"dependencyGraphCells\": [[3, 1]]" +
        "}" +
        "}";
    viewportUrl = createViewport(clientId, viewportDefJson);
    if (StringUtils.isEmpty(viewportUrl)) {
      Assert.fail("no URL returned for 2nd viewport");
    }
    latestResult = readFromPath(viewportUrl + "/data");
    jsonResults = new JSONObject(latestResult);
    assertEquals(2, jsonResults.length());

    row2 = jsonResults.getJSONArray("2");
    assertEquals(3, row2.length());
    assertEquals(3, row2.getInt(0));
    assertEquals(6, row2.getInt(1));
    assertEquals(9, row2.getInt(2));

    JSONArray row3 = jsonResults.getJSONArray("3");
    assertEquals(3, row3.length());
    assertEquals(4, row3.getInt(0));
    assertEquals(8, row3.getInt(1));
    assertEquals(12, row3.getInt(2));
  }

  private static String createViewport(String clientId, String viewportDefJson) throws IOException {
    String viewportUrl;
    BufferedReader reader = null;
    BufferedWriter writer = null;
    try {
      URL url = new URL("http://localhost:8080/rest/viewports?clientId=" + clientId);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("CONTENT-TYPE", MediaType.APPLICATION_JSON);
      connection.connect();
      writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
      writer.write(viewportDefJson);
      writer.flush();
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      viewportUrl = reader.readLine();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          s_logger.warn("failed to close reader", e);
        }
      }
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          s_logger.warn("failed to close writer", e);
        }
      }
    }
    return viewportUrl;
  }
}