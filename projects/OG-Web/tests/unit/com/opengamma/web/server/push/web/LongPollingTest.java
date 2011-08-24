/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.subscription.TestRestUpdateManager;
import org.eclipse.jetty.server.Server;
import org.json.JSONException;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.opengamma.web.server.push.web.WebPushTestUtils.checkJsonResults;
import static com.opengamma.web.server.push.web.WebPushTestUtils.handshake;
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
  private TestRestUpdateManager _updateManager;
  private LongPollingConnectionManager _longPollingConnectionManager;

  @BeforeClass
  void createJettyServer() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/long-poll-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    _updateManager = context.getBean(TestRestUpdateManager.class);
    _longPollingConnectionManager = context.getBean(LongPollingConnectionManager.class);
  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }

  @Test
  public void testHandshake() throws IOException {
    String clientId = handshake();
    assertEquals(CLIENT_ID, clientId);
  }

  /**
   * Tests sending an update to a client that is blocked on a long poll request
   */
  @Test
  public void longPollBlocking() throws IOException, ExecutionException, InterruptedException, JSONException {
    final String clientId = handshake();
    new Thread(new Runnable() {
      @Override
      public void run() {
        waitAndSend(clientId, RESULT1);
      }
    }).start();
    String result = readFromPath("/updates/" + clientId);
    checkJsonResults(result, RESULT1);
  }

  /**
   * Tests sending a single update to a client's connection when it's not connected and then connecting.
   */
  @Test
  public void longPollNotBlocking() throws IOException, JSONException {
    String clientId = handshake();
    _updateManager.sendUpdate(RESULT1);
    String result = readFromPath("/updates/" + clientId);
    checkJsonResults(result, RESULT1);
  }

  /**
   * Tests sending multiple updates to a connection where the client isn't currently connected.
   */
  @Test
  public void longPollQueue() throws IOException, JSONException {
    String clientId = handshake();
    _updateManager.sendUpdate(RESULT1);
    _updateManager.sendUpdate(RESULT2);
    _updateManager.sendUpdate(RESULT3);
    String result = readFromPath("/updates/" + clientId);
    checkJsonResults(result, RESULT1, RESULT2, RESULT3);
  }

  /**
   * test multiple updates for the same url get squashed into a single update
   */
  @Test
  public void longPollQueueMultipleUpdates() throws IOException, JSONException {
    String clientId = handshake();
    _updateManager.sendUpdate(RESULT1);
    _updateManager.sendUpdate(RESULT1);
    _updateManager.sendUpdate(RESULT2);
    _updateManager.sendUpdate(RESULT3);
    _updateManager.sendUpdate(RESULT2);
    String result = readFromPath("/updates/" + clientId);
    checkJsonResults(result, RESULT1, RESULT2, RESULT3);
  }

  @Test
  public void repeatingLongPoll() throws IOException, JSONException {
    final String clientId = handshake();
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
    checkJsonResults(readFromPath(path), RESULT1);
    checkJsonResults(readFromPath(path), RESULT2);
    checkJsonResults(readFromPath(path), RESULT3);
    checkJsonResults(readFromPath(path), RESULT2);
    checkJsonResults(readFromPath(path), RESULT1);
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
    _updateManager.sendUpdate(result);
  }
}