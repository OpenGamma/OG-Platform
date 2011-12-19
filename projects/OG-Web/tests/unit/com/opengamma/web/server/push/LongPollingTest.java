/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.util.tuple.Pair;
import org.eclipse.jetty.server.Server;
import org.json.JSONException;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.testng.AssertJUnit.assertEquals;

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
  private TestConnectionManager _updateManager;
  private LongPollingConnectionManager _longPollingConnectionManager;

  @BeforeClass
  void createJettyServer() throws Exception {
    Pair<Server, WebApplicationContext> serverAndContext =
        WebPushTestUtils.createJettyServer("classpath:/com/opengamma/web/server/push/long-poll-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    _updateManager = context.getBean(TestConnectionManager.class);
    _longPollingConnectionManager = context.getBean(LongPollingConnectionManager.class);
  }

  @AfterClass
  void shutdownJettyServer() throws Exception {
    _server.stop();
  }

  @Test
  public void testHandshake() throws IOException {
    String clientId = WebPushTestUtils.handshake();
    assertEquals(CLIENT_ID, clientId);
  }

  /**
   * Tests sending an update to a client that is blocked on a long poll request
   */
  @Test
  public void longPollBlocking() throws IOException, ExecutionException, InterruptedException, JSONException {
    final String clientId = WebPushTestUtils.handshake();
    new Thread(new Runnable() {
      @Override
      public void run() {
        waitAndSend(clientId, RESULT1);
      }
    }).start();
    String result = WebPushTestUtils.readFromPath("/updates/" + clientId);
    WebPushTestUtils.checkJsonResults(result, RESULT1);
  }

  /**
   * Tests sending a single update to a client's connection when it's not connected and then connecting.
   */
  @Test
  public void longPollNotBlocking() throws IOException, JSONException {
    String clientId = WebPushTestUtils.handshake();
    _updateManager.sendUpdate(RESULT1);
    String result = WebPushTestUtils.readFromPath("/updates/" + clientId);
    WebPushTestUtils.checkJsonResults(result, RESULT1);
  }

  /**
   * Tests sending multiple updates to a connection where the client isn't currently connected.
   */
  @Test
  public void longPollQueue() throws IOException, JSONException {
    String clientId = WebPushTestUtils.handshake();
    _updateManager.sendUpdate(RESULT1);
    _updateManager.sendUpdate(RESULT2);
    _updateManager.sendUpdate(RESULT3);
    String result = WebPushTestUtils.readFromPath("/updates/" + clientId);
    WebPushTestUtils.checkJsonResults(result, RESULT1, RESULT2, RESULT3);
  }

  /**
   * test multiple updates for the same url get squashed into a single update
   */
  @Test
  public void longPollQueueMultipleUpdates() throws IOException, JSONException {
    String clientId = WebPushTestUtils.handshake();
    _updateManager.sendUpdate(RESULT1);
    _updateManager.sendUpdate(RESULT1);
    _updateManager.sendUpdate(RESULT2);
    _updateManager.sendUpdate(RESULT3);
    _updateManager.sendUpdate(RESULT2);
    String result = WebPushTestUtils.readFromPath("/updates/" + clientId);
    WebPushTestUtils.checkJsonResults(result, RESULT1, RESULT2, RESULT3);
  }

  @Test
  public void repeatingLongPoll() throws IOException, JSONException {
    final String clientId = WebPushTestUtils.handshake();
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
    WebPushTestUtils.checkJsonResults(WebPushTestUtils.readFromPath(path), RESULT1);
    WebPushTestUtils.checkJsonResults(WebPushTestUtils.readFromPath(path), RESULT2);
    WebPushTestUtils.checkJsonResults(WebPushTestUtils.readFromPath(path), RESULT3);
    WebPushTestUtils.checkJsonResults(WebPushTestUtils.readFromPath(path), RESULT2);
    WebPushTestUtils.checkJsonResults(WebPushTestUtils.readFromPath(path), RESULT1);
  }

  @Test
  public void longPollTimeout() throws IOException, JSONException {
    String clientId = WebPushTestUtils.handshake();
    String path = "/updates/" + clientId;
    String timeoutResult = WebPushTestUtils.readFromPath(path);
    assertEquals("", timeoutResult);
    _updateManager.sendUpdate(RESULT1);
    WebPushTestUtils.checkJsonResults(WebPushTestUtils.readFromPath(path), RESULT1);
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