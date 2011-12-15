/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import org.eclipse.jetty.server.Server;
import org.json.JSONException;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.time.Instant;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.opengamma.web.server.push.WebPushTestUtils.checkJsonResults;
import static com.opengamma.web.server.push.WebPushTestUtils.createJettyServer;
import static com.opengamma.web.server.push.WebPushTestUtils.handshake;
import static com.opengamma.web.server.push.WebPushTestUtils.readFromPath;
import static org.testng.AssertJUnit.assertEquals;

/**
 *
 */
public class RestEntitySubscriptionTest {

  private final String _uidStr = "Tst~101";
  private final UniqueId _uid = UniqueId.parse(_uidStr);
  private final UniqueId _uidV1 = _uid.withVersion("1");
  private final UniqueId _uidV2 = _uid.withVersion("2");

  private Server _server;
  private TestChangeManager _changeManager;

  @BeforeClass
  public void createServer() throws Exception {
    Pair<Server,WebApplicationContext> serverAndContext = createJettyServer("classpath:/com/opengamma/web/server/push/rest-subscription-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    _changeManager = context.getBean("changeManager", TestChangeManager.class);
  }

  @AfterClass
  public void tearDown() throws Exception {
    _server.stop();
  }

  @Test
  public void entitySubscription() throws IOException, JSONException {
    String clientId = handshake();
    String restUrl = "/jax/test/" + _uidStr;
    // this REST request should set up a subscription for object ID Tst~101
    readFromPath(restUrl, clientId);
    // send a change event
    _changeManager.entityChanged(ChangeType.UPDATED, _uidV1, _uidV2, Instant.now());
    // connect to the long-polling URL to receive notification of the change
    String json = readFromPath("/updates/" + clientId);
    checkJsonResults(json, restUrl);
  }

  @Test
  public void subResourceSubscription() throws IOException, JSONException {
    String clientId = handshake();
    String restUrl = "/jax/testsub/" + _uidStr;
    readFromPath(restUrl, clientId);
    _changeManager.entityChanged(ChangeType.UPDATED, _uidV1, _uidV2, Instant.now());
    String json = readFromPath("/updates/" + clientId);
    checkJsonResults(json, restUrl);
  }

  @Test
  public void multipleEntitySubscription() throws IOException, JSONException {
    String clientId = handshake();
    String restUrl1 = "/jax/test/" + _uidStr;
    String uid2Str = "Tst~102";
    UniqueId uid2 = UniqueId.parse(uid2Str);
    UniqueId uid2V1 = uid2.withVersion("1");
    UniqueId uid2V2 = uid2.withVersion("2");
    String restUrl2 = "/jax/test/" + uid2Str;
    readFromPath(restUrl1, clientId);
    readFromPath(restUrl2, clientId);
    _changeManager.entityChanged(ChangeType.UPDATED, _uidV1, _uidV2, Instant.now());
    _changeManager.entityChanged(ChangeType.UPDATED, uid2V1, uid2V2, Instant.now());
    String json = readFromPath("/updates/" + clientId);
    checkJsonResults(json, restUrl1, restUrl2);
  }

  @Test
  public void noClientIdNoSubscription() throws IOException {
    String clientId = handshake();
    String restUrl = "/jax/test/" + _uidStr;
    // this REST request shouldn't set up a subscription because there is no client ID so the server doesn't know
    // where to send the update
    readFromPath(restUrl);
    // send a change event that we should never see
    _changeManager.entityChanged(ChangeType.UPDATED, _uidV1, _uidV2, Instant.now());
    String result = readFromPath("/updates/" + clientId);
    assertEquals("", result);
  }

  @Test(expectedExceptions = FileNotFoundException.class)
  public void invalidClientId() throws IOException {
    String restUrl = "/jax/test/" + _uidStr;
    // this REST request shouldn't set up a subscription because the client ID doesn't match an existing client connection
    readFromPath(restUrl);
    // send a change event that we should never see
    _changeManager.entityChanged(ChangeType.UPDATED, _uidV1, _uidV2, Instant.now());
    // will throw an exception because the URL is unknown
    readFromPath("/updates/abc");
  }

  // TODO confirm the correct behaviour - presumably the REST request would fail so maybe the filter should look at the response status
  // what is the response status in that case?
  /*@Test
  public void invalidUniqueId() {
  }*/
}