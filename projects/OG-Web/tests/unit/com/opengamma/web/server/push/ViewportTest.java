/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.util.tuple.Pair;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 *
 */
public class ViewportTest {

  private Server _server;

  @BeforeClass
  void createJettyServer() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/server/push/viewport-test.xml");
    _server = serverAndContext.getFirst();
  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }

  @Test
  public void viewportData() throws Exception {
    String clientId = WebPushTestUtils.handshake();
    String viewportDefJson = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [0, 1, 2], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[1, 2]]" +
        "}" +
        "}";
    String viewportUrl = WebPushTestUtils.createViewport(clientId, viewportDefJson);
    if (StringUtils.isEmpty(viewportUrl)) {
      Assert.fail("no URL returned for viewport");
    }
    String latestResult = WebPushTestUtils.readFromPath(viewportUrl + "/data");
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
    String clientId = WebPushTestUtils.handshake();
    String viewportDefJson = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [0, 1, 2], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[1, 2]]" +
        "}" +
        "}";
    String viewportUrl1 = WebPushTestUtils.createViewport(clientId, viewportDefJson);
    if (StringUtils.isEmpty(viewportUrl1)) {
      Assert.fail("no URL returned for 1st viewport");
    }
    String latestResult = WebPushTestUtils.readFromPath(viewportUrl1 + "/data");
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
        "\"rowIds\": [2, 3], " +
        "\"lastTimestamps\": [12345688, 12345689], " +
        "\"dependencyGraphCells\": [[3, 1]]" +
        "}" +
        "}";
    String viewportUrl2 = WebPushTestUtils.createViewport(clientId, viewportDefJson);
    if (StringUtils.isEmpty(viewportUrl2)) {
      Assert.fail("no URL returned for 2nd viewport");
    }
    latestResult = WebPushTestUtils.readFromPath(viewportUrl2 + "/data");
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

    // TODO check that the original URL is no longer valid
  }

}