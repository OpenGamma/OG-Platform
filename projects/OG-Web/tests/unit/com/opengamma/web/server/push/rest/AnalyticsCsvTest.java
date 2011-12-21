/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.WebPushTestUtils;
import org.json.JSONException;

import java.io.IOException;

/**
 * Tests getting analytics as CSV from a real engine.  Requires an engine running on {@code localhost}.
 */
public class AnalyticsCsvTest {

  public static void main(String[] args) throws IOException, JSONException, InterruptedException {
    String clientId = WebPushTestUtils.handshake();
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
    String viewportUrl = WebPushTestUtils.createViewport(clientId, viewDefJson);
    //noinspection InfiniteLoopStatement
    while (true) {
      String csv = WebPushTestUtils.readFromPath(viewportUrl + "/report/csv");
      System.out.println(csv);

      Thread.sleep(2000);
    }
  }
}
