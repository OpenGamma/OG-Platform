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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.opengamma.web.server.push.web.WebPushTestUtils.readFromPath;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests pushing results to a long polling HTTP connection.
 */
@Test
public class LongPollingTest {

  public static final String CLIENT_ID = "CLIENT_ID";

  private static final String RESULT1 = "RESULT1";
  private static final String RESULT2 = "RESULT2";
  private static final String RESULT3 = "RESULT3";

  private Server _server;
  private TestRestUpdateManager _subscriptionManager;
  private LongPollingConnectionManager _longPollingConnectionManager;

  @BeforeClass
  void createJettyServer() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/long-poll-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    _subscriptionManager = context.getBean(TestRestUpdateManager.class);
    _longPollingConnectionManager = context.getBean(LongPollingConnectionManager.class);
  }

  @Test
  public void handshake() throws IOException {
    String clientId = readFromPath("/handshake");
    assertEquals(CLIENT_ID, clientId);
  }

  /**
   * Tests sending an update to a client that is blocked on a long poll request
   */
  @Test
  public void longPollBlocking() throws IOException, ExecutionException, InterruptedException {
    final String clientId = readFromPath("/handshake");
    new Thread(new Runnable() {
      @Override
      public void run() {
        waitAndSend(clientId, RESULT1);
      }
    }).start();
    String result = readFromPath("/updates/" + clientId);
    assertEquals(RESULT1, result);
  }

  /**
   * Tests sending a single update to a client's connection when it's not connected and then connecting.
   */
  @Test
  public void longPollNotBlocking() throws IOException {
    String clientId = readFromPath("/handshake");
    _subscriptionManager.sendUpdate(RESULT1);
    String result = readFromPath("/updates/" + clientId);
    assertEquals(RESULT1, result);
  }

  /**
   * Tests sending multiple updates to a connection where the client isn't currently connected.
   */
  @Test
  public void longPollQueue() throws IOException {
    String clientId = readFromPath("/handshake");
    _subscriptionManager.sendUpdate(RESULT1);
    _subscriptionManager.sendUpdate(RESULT2);
    _subscriptionManager.sendUpdate(RESULT3);
    String result = readFromPath("/updates/" + clientId);
    // can't depend on the order when multiple updates are sent at once
    List<String> results = Arrays.asList(result.split("\n"));
    assertEquals(3, results.size());
    assertTrue(results.contains(RESULT1));
    assertTrue(results.contains(RESULT2));
    assertTrue(results.contains(RESULT3));
  }

  /**
   * test multiple updates for the same url get squashed into a single update
   */
  @Test
  public void longPollQueueMultipleUpdates() throws IOException {
    String clientId = readFromPath("/handshake");
    _subscriptionManager.sendUpdate(RESULT1);
    _subscriptionManager.sendUpdate(RESULT1);
    _subscriptionManager.sendUpdate(RESULT2);
    _subscriptionManager.sendUpdate(RESULT3);
    _subscriptionManager.sendUpdate(RESULT2);
    String result = readFromPath("/updates/" + clientId);
    // can't depend on the order when multiple updates are sent at once
    List<String> results = Arrays.asList(result.split("\n"));
    assertEquals(3, results.size());
    assertTrue(results.contains(RESULT1));
    assertTrue(results.contains(RESULT2));
    assertTrue(results.contains(RESULT3));
  }

  @Test
  public void repeatingLongPoll() throws IOException {
    final String clientId = readFromPath("/handshake");
    new Thread(new Runnable() {
      @Override
      public void run() {
        waitAndSend(clientId, RESULT1);
        waitAndSend(clientId, RESULT2);
        waitAndSend(clientId, RESULT3);
        waitAndSend(clientId, RESULT2);
        waitAndSend(clientId, RESULT1);
      }
    }).start();
    String path = "/updates/" + clientId;
    assertEquals(RESULT1, readFromPath(path));
    assertEquals(RESULT2, readFromPath(path));
    assertEquals(RESULT3, readFromPath(path));
    assertEquals(RESULT2, readFromPath(path));
    assertEquals(RESULT1, readFromPath(path));
  }

  /**
   * Waits until the client is connected before sending the result to its listener
   */
  private void waitAndSend(String clientId, String result) {
    // wait for the request to block
    while (!_longPollingConnectionManager.isClientConnected(clientId)) {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    _subscriptionManager.sendUpdate(result);
  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }
}