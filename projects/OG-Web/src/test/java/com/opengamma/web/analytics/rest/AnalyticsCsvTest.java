/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.io.IOException;

import org.json.JSONException;

import com.opengamma.web.analytics.push.WebPushTestUtils;

/**
 * Tests getting analytics as CSV from a real engine.  Requires an engine running on {@code localhost}.
 */
public class AnalyticsCsvTest {

  public static void main(String[] args) throws IOException, JSONException, InterruptedException {
    WebPushTestUtils _webPushTestUtils = new WebPushTestUtils();
    String clientId = _webPushTestUtils.handshake();
    String viewDefJson = "{" +
        "\"viewDefinitionName\": \"Single Swap Test View\", " +
        //"\"snapshotId\": \"Tst~123\", " + // use live data
        "\"portfolioViewport\": {" +
        "\"rowIds\": [0, 1, 2, 3], " +
        "\"lastTimestamps\": [null, null, null, null], " +
        "\"dependencyGraphCells\": [[0, 0], [1, 2]]" +
        "}," +
        "\"primitivesViewport\": {" +
        "\"rowIds\": [0, 1, 2, 3], " +
        "\"lastTimestamps\": [null, null, null, null], " +
        "\"dependencyGraphCells\": [[0, 0]]" +
        "}" +
        "}";
    String viewportUrl = _webPushTestUtils.createViewport(clientId, viewDefJson);
    //noinspection InfiniteLoopStatement
    while (true) {
      String csv = _webPushTestUtils.readFromPath(viewportUrl + "/report/csv");
      System.out.println(csv);

      Thread.sleep(2000);
    }
  }
}
