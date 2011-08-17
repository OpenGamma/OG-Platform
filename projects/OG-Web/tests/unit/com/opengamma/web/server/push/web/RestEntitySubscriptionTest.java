/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.core.change.ChangeType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.test.TestChangeManager;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.WebApplicationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.time.Instant;
import java.io.IOException;

import static com.opengamma.web.server.push.web.WebPushTestUtils.readFromPath;
import static org.testng.AssertJUnit.assertEquals;

/**
 *
 */
@Test
public class RestEntitySubscriptionTest {

  private Server _server;
  private TestChangeManager _changeManager;

  @BeforeClass
  public void createJettyServer() throws Exception {
    Pair<Server,WebApplicationContext> serverAndContext = WebPushTestUtils.createJettyServer(
        "classpath:/com/opengamma/web/rest-subscription-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    _changeManager = context.getBean(TestChangeManager.class);
  }

  @AfterClass
  public void tearDown() throws Exception {
    _server.stop();
  }

  @Test
  public void entitySubscription() throws IOException {
    String uidStr = "Tst~101";
    UniqueId uid = UniqueId.parse(uidStr);
    UniqueId uidV1 = uid.withVersion("1");
    UniqueId uidV2 = uid.withVersion("2");
    String clientId = readFromPath("handshake");
    // TODO confirm the exact form of the URLs. where are they relative to?
    String restUrlBase = "rest/";
    String restUrl = "test/" + uidStr;
    String restUrlFull = restUrlBase + restUrl;
    // this REST request should set up a subscription for object ID Tst~101
    readFromPath(restUrlFull, clientId);
    // send a change event
    _changeManager.entityChanged(ChangeType.UPDATED, uidV1, uidV2, Instant.now());
    // connect to the long-polling URL to receive notification of the change
    String result = readFromPath("updates/" + clientId);
    assertEquals(restUrl, result);
  }

  @Test
  public void subResourceSubscription() {
    // TODO implement RestSubscriptionTest.subResourceSubscription()
    throw new UnsupportedOperationException("subResourceSubscription not implemented");
  }

  @Test
  public void subResourceNoSubscription() {
    // TODO implement RestSubscriptionTest.subResourceNoSubscription()
    throw new UnsupportedOperationException("subResourceNoSubscription not implemented");
  }

  @Test
  public void noClientIdNoSubscription() {
    // TODO implement RestSubscriptionTest.noClientIdNoSubscription()
    throw new UnsupportedOperationException("noClientIdNoSubscription not implemented");
  }

  @Test
  public void invalidClientId() {
    // TODO implement RestSubscriptionTest.invalidClientId()
    throw new UnsupportedOperationException("invalidClientId not implemented");
  }

  @Test
  public void invalidUniqueId() {
    // TODO implement RestSubscriptionTest.invalidUniqueId()
    throw new UnsupportedOperationException("invalidUniqueId not implemented");
  }
}