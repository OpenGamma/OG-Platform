package com.opengamma.web.server.push.web;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.opengamma.web.server.push.web.WebPushTestUtils.createViewport;
import static com.opengamma.web.server.push.web.WebPushTestUtils.handshake;
import static com.opengamma.web.server.push.web.WebPushTestUtils.readFromPath;

/**
 * Tests live updating of analytics from a real engine.
 */
public class EngineAnalyticsTest {

  public static void main(String[] args) throws IOException, JSONException {
    String clientId = handshake();
    String viewDefJson = "{" +
        "\"viewDefinitionName\": \"Simple Cash Test View\", " +
        //"\"snapshotId\": \"Tst~123\", " + // use live data
        "\"portfolioViewport\": {" +
        "\"rows\": [[0, null], [1, null], [2, null], [3, null]], " +
        "\"dependencyGraphCells\": []" +
        "}" +
        "}";
    String viewportUrl = createViewport(clientId, viewDefJson);
    // need to request data to activate the subscription
    String firstResults = readFromPath(viewportUrl + "/data", clientId);
    System.out.println("first results: " + firstResults);
    while (true) {
      String urlJson = readFromPath("/updates/" + clientId);
      System.out.println("updates: " + urlJson);
      if (!StringUtils.isEmpty(urlJson)) {
        JSONObject urlsObject = new JSONObject(urlJson);
        JSONArray updates = urlsObject.getJSONArray("updates");
        for (int i = 0; i < updates.length(); i++) {
          String url = updates.getString(i);
          String results = readFromPath(url, clientId);
          System.out.println("url: " + url + ", results: " + results);
        }
      }
    }
  }
}