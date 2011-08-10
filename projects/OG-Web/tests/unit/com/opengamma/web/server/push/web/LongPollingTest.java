/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.google.common.base.Joiner;
import com.opengamma.web.server.push.subscription.TestSubscriptionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.AssertJUnit.assertEquals;

/**
 *
 */
@Test
public class LongPollingTest {

  public static final String CLIENT_ID = "CLIENT_ID";

  private static final String s_urlBase = "http://localhost:8080/";

  private Server _server;
  private static final String RESULT1 = "RESULT1";
  private static final String RESULT2 = "RESULT2";
  private static final String RESULT3 = "RESULT3";
  private TestSubscriptionManager _subscriptionManager;
  private LongPollingConnectionManager _longPollingConnectionManager;

  private static URL url(String path) throws MalformedURLException {
    return new URL(s_urlBase + path);
  }

  private String readFromPath(String path) throws IOException {
    BufferedReader reader = null;
    StringBuilder builder;
    try {
      char[] chars = new char[512];
      builder = new StringBuilder();
      reader = new BufferedReader(new InputStreamReader(url(path).openStream()));
      int bytesRead;
      while ((bytesRead = reader.read(chars)) != -1) {
        builder.append(chars, 0, bytesRead);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return builder.toString();
  }

  @BeforeClass
  void createJettyServer() throws Exception {
    // TODO this stuff probably won't work in bamboo - need to find out what the pwd is for the tests
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(8080);
    WebAppContext context = new WebAppContext();
    context.setContextPath("/");
    context.setResourceBase("build/classes");
    context.setDescriptor("web-push/WEB-INF/web.xml");
    Map<String, String> params = new HashMap<String, String>();
    params.put("contextConfigLocation", "classpath:/com/opengamma/web/test-web-push.xml");
    context.setInitParams(params);
    context.addEventListener(new ContextLoaderListener());
    _server = new Server();
    _server.addConnector(connector);
    _server.setHandler(context);
    _server.start();
    WebApplicationContext springContext = (WebApplicationContext) context.getServletContext().getAttribute(
            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    _subscriptionManager = springContext.getBean(TestSubscriptionManager.class);
    _longPollingConnectionManager = springContext.getBean(LongPollingConnectionManager.class);
  }

  @Test
  public void handshake() throws IOException {
    String result = readFromPath("handshake");
    assertEquals(CLIENT_ID, result);
  }

  /** Tests sending an update to a client that is blocked on a long poll request */
  @Test
  public void longPollBlocking() throws IOException, ExecutionException, InterruptedException {
    final String clientId = readFromPath("handshake");
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<String> future = executor.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return readFromPath("subscription?clientId=" + clientId);
      }
    });
    // wait for the request to block
    while (!_longPollingConnectionManager.isClientConnected(clientId)) {
      Thread.sleep(1000);
    }
    _subscriptionManager.sendUpdate(RESULT1);
    String result = future.get();
    assertEquals(RESULT1, result);
  }

  @Test
  public void longPollNotBlocking() throws IOException {
    final String clientId = readFromPath("handshake");
    _subscriptionManager.sendUpdate(RESULT1);
    String result = readFromPath("subscription?clientId=" + clientId);
    assertEquals(RESULT1, result);
  }

  /** Tests sending multiple updates to a connection where the client isn't currently connected. */
  @Test
  public void longPollQueue() throws IOException {
    final String clientId = readFromPath("handshake");
    _subscriptionManager.sendUpdate(RESULT1);
    _subscriptionManager.sendUpdate(RESULT2);
    _subscriptionManager.sendUpdate(RESULT3);
    String result = readFromPath("subscription?clientId=" + clientId);
    assertEquals(RESULT1 + "\n" + RESULT2 + "\n" + RESULT3, result);
  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }
}