/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.ContextLoaderListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 *
 */
public class LongPollingTest {

  public static final String CLIENT_ID = "CLIENT_ID";

  private Server _server;

  @BeforeClass
  void createJettyServer() throws Exception {
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(8080);
    WebAppContext context = new WebAppContext();
    context.setContextPath("/");
    // TODO is a resource base needed if there are no resources? what about the classpath?
    // TODO IDEA project should build to the same place
    context.setResourceBase("build/classes");
    // TODO is this relative to the resource base? or the pwd?
    context.setDescriptor("web-push/WEB-INF/web.xml");
    Map<String, String> params = new HashMap<String, String>();
    params.put("contextConfigLocations", "classpath:/com/opengamma/web/test-web-push.xml"); // TODO this doesn't exist yet
    context.setInitParams(params);
    context.addEventListener(new ContextLoaderListener());
    _server = new Server();
    _server.addConnector(connector);
    _server.setHandler(context);
    _server.start();
  }

  @Test
  void handshake() throws IOException {
    URL url = new URL("http://localhost:8080/handshake");
    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    String clientId = reader.readLine();
    assertEquals(CLIENT_ID, clientId);
  }

  @Test
  void longPoll() {

  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }
}