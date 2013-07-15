/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import static org.testng.AssertJUnit.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.json.JSONException;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RestEntitySubscriptionTest {

  private final String _uidStr = "Tst~101";
  private final UniqueId _uid = UniqueId.parse(_uidStr);
  private final UniqueId _uidV1 = _uid.withVersion("1");

  private Server _server;
  private TestChangeManager _changeManager;
  private WebPushTestUtils _webPushTestUtils = new WebPushTestUtils();

  @BeforeClass
  public void createServer() throws Exception {
    Pair<Server,WebApplicationContext> serverAndContext = _webPushTestUtils.createJettyServer("classpath:/com/opengamma/web/analytics/push/rest-subscription-test.xml");
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
    String clientId = _webPushTestUtils.handshake();
    String restUrl = "/jax/test/" + _uidStr;
    // this REST request should set up a subscription for object ID Tst~101
    _webPushTestUtils.readFromPath(restUrl, clientId);
    // send a change event
    _changeManager.entityChanged(ChangeType.CHANGED, _uidV1.getObjectId(), null, null, Instant.now());
    // connect to the long-polling URL to receive notification of the change
    String json = _webPushTestUtils.readFromPath("/updates/" + clientId);
    WebPushTestUtils.checkJsonResults(json, restUrl);
  }

  @Test
  public void subResourceSubscription() throws IOException, JSONException {
    String clientId = _webPushTestUtils.handshake();
    String restUrl = "/jax/testsub/" + _uidStr;
    _webPushTestUtils.readFromPath(restUrl, clientId);
    _changeManager.entityChanged(ChangeType.CHANGED, _uidV1.getObjectId(), null, null, Instant.now());
    String json = _webPushTestUtils.readFromPath("/updates/" + clientId);
    WebPushTestUtils.checkJsonResults(json, restUrl);
  }

  @Test
  public void multipleEntitySubscription() throws IOException, JSONException {
    String clientId = _webPushTestUtils.handshake();
    String restUrl1 = "/jax/test/" + _uidStr;
    String uid2Str = "Tst~102";
    UniqueId uid2 = UniqueId.parse(uid2Str);
    UniqueId uid2V1 = uid2.withVersion("1");
    String restUrl2 = "/jax/test/" + uid2Str;
    _webPushTestUtils.readFromPath(restUrl1, clientId);
    _webPushTestUtils.readFromPath(restUrl2, clientId);
    _changeManager.entityChanged(ChangeType.CHANGED, _uidV1.getObjectId(), null, null, Instant.now());
    _changeManager.entityChanged(ChangeType.CHANGED, uid2V1.getObjectId(), null, null, Instant.now());
    String json = _webPushTestUtils.readFromPath("/updates/" + clientId);
    WebPushTestUtils.checkJsonResults(json, restUrl1, restUrl2);
  }

  @Test
  public void noClientIdNoSubscription() throws IOException {
    String clientId = _webPushTestUtils.handshake();
    String restUrl = "/jax/test/" + _uidStr;
    // this REST request shouldn't set up a subscription because there is no client ID so the server doesn't know
    // where to send the update
    _webPushTestUtils.readFromPath(restUrl);
    // send a change event that we should never see
    _changeManager.entityChanged(ChangeType.CHANGED, _uidV1.getObjectId(), null, null, Instant.now());
    String result = _webPushTestUtils.readFromPath("/updates/" + clientId);
    assertEquals("", result);
  }

  @Test(expectedExceptions = FileNotFoundException.class)
  public void invalidClientId() throws IOException {
    String restUrl = "/jax/test/" + _uidStr;
    // this REST request shouldn't set up a subscription because the client ID doesn't match an existing client connection
    _webPushTestUtils.readFromPath(restUrl);
    // send a change event that we should never see
    _changeManager.entityChanged(ChangeType.CHANGED, _uidV1.getObjectId(), null, null, Instant.now());
    // will throw an exception because the URL is unknown
    _webPushTestUtils.readFromPath("/updates/abc");
  }

  // TODO confirm the correct behaviour - presumably the REST request would fail so maybe the filter should look at the response status
  // what is the response status in that case?
  /*@Test
  public void invalidUniqueId() {
  }*/
}
