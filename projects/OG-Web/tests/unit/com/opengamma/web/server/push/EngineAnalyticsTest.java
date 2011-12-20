package com.opengamma.web.server.push;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Tests live updating of analytics from a real engine.  Requires an engine running on {@code localhost}.
 */
public class EngineAnalyticsTest {

  public static void main(String[] args) throws IOException, JSONException {
    String clientId = WebPushTestUtils.handshake();
    String viewDefJson = "{" +
        "\"viewDefinitionName\": \"Single Swap Test View\", " +
        //"\"snapshotId\": \"Tst~123\", " + // use live data
        "\"portfolioViewport\": {" +
        "\"rowIds\": [0, 1, 2, 3], " +
        "\"lastTimestamps\": [null, null, null, null], " +
        "\"dependencyGraphCells\": [[0, 0], [1, 1]]" +
        "}" +
        "}";
    String viewportUrl = WebPushTestUtils.createViewport(clientId, viewDefJson);
    // need to request data to activate the subscription
    String firstResults = WebPushTestUtils.readFromPath(viewportUrl + "/data", clientId);
    System.out.println("first results: " + firstResults);
    //noinspection InfiniteLoopStatement
    while (true) {
      String urlJson = WebPushTestUtils.readFromPath("/updates/" + clientId);
      System.out.println("updates: " + urlJson);
      if (!StringUtils.isEmpty(urlJson)) {
        JSONObject urlsObject = new JSONObject(urlJson);
        JSONArray updates = urlsObject.getJSONArray("updates");
        for (int i = 0; i < updates.length(); i++) {
          String url = updates.getString(i);
          String results = WebPushTestUtils.readFromPath(url, clientId);
          System.out.println("url: " + url + ", results: " + results);
        }
      }
    }
  }
}