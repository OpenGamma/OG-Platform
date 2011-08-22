/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.subscription.TestRestUpdateManager;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static com.opengamma.web.server.push.web.WebPushTestUtils.readFromPath;

/**
 *
 */
public class ViewportTest {

  private Server _server;

  @BeforeClass
  void createJettyServer() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/viewport-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }

  @Test
  public void createViewport() throws Exception {
    String clientId = readFromPath("/handshake");
    URL url = new URL("http://localhost:8080/rest/viewports");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    // TODO need to write viewport definition JSON to the reqest body
    // TODO need to set MIME type to application/json
  }
}