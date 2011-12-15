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
      String portfolioCsv = WebPushTestUtils.readFromPath(viewportUrl + "/portfolio/analytics");
      System.out.println("portfolioCsv: " + portfolioCsv);

      // TODO dependency graph CSV isn't implemented yet, this is always empty
      // dep graph grid exists but doesn't return CSV
      String portfolioDepGraphCsv = WebPushTestUtils.readFromPath(viewportUrl + "/portfolio/1/2");
      System.out.println("portfolioDepGraphCsv: " + portfolioDepGraphCsv);
      
      String primitivesCsv = WebPushTestUtils.readFromPath(viewportUrl + "/primitives/analytics");
      System.out.println("primitivesCsv: " + primitivesCsv);

      // TODO dependency graph CSV isn't implemented yet, this would return 404
      // dep graph grid doesn't exist
      /*String primitivesDepGraphCsv = WebPushTestUtils.readFromPath(viewportUrl + "/primitives/0/0");
      System.out.println("primitivesDepGraphCsv: " + primitivesDepGraphCsv);*/

      Thread.sleep(2000);
    }
  }
}
