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
import java.io.IOException;

import static com.opengamma.web.server.push.WebPushTestUtils.checkJsonResults;
import static com.opengamma.web.server.push.WebPushTestUtils.createJettyServer;
import static com.opengamma.web.server.push.WebPushTestUtils.handshake;
import static com.opengamma.web.server.push.WebPushTestUtils.readFromPath;

public class RestMasterSubscriptionTest {

  private Server _server;
  private TestChangeManager _positionChangeManager;

  @BeforeClass
  public void createServer() throws Exception {
    Pair<Server,WebApplicationContext> serverAndContext = createJettyServer("classpath:/com/opengamma/web/server/push/rest-subscription-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    _positionChangeManager = context.getBean("positionChangeManager", TestChangeManager.class);
  }

  @AfterClass
  public void tearDown() throws Exception {
    _server.stop();
  }

  @Test
  public void masterSubscription() throws IOException, JSONException {
    String clientId = handshake();
    String restUrl = "/jax/test/positions";
    // this REST request should set up a subscription for any changes in the position master
    readFromPath(restUrl, clientId);
    // send a change event
    UniqueId uid = UniqueId.of("Tst", "101");
    _positionChangeManager.entityChanged(ChangeType.UPDATED, uid, uid, Instant.now());
    // connect to the long-polling URL to receive notification of the change
    String json = readFromPath("/updates/" + clientId);
    checkJsonResults(json, restUrl);
  }
}