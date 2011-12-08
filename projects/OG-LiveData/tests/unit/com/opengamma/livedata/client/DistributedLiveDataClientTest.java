/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.MockLiveDataServer;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.livedata.test.LiveDataClientTestUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class DistributedLiveDataClientTest {

  public static final ExternalScheme TEST_IDENTIFICATION_SCHEME = ExternalScheme.of("bar");
  public static final LiveDataSpecification TEST_LIVE_DATA_SPEC = new LiveDataSpecification("Foo", ExternalId.of(TEST_IDENTIFICATION_SCHEME, "baz"));

  private static String TEST_ID_1 = "id1";
  private static String TEST_ID_2 = "id2";
  private static UserPrincipal TEST_USER = new UserPrincipal("alice", "127.0.0.1");
  
  private MockLiveDataServer _server;
  private DistributedLiveDataClient _client;
  
  private MutableFudgeMsg[] _testMsgs; 
  
  @BeforeMethod
  public void initialize() {
    MutableFudgeMsg testMsg1 = OpenGammaFudgeContext.getInstance().newMessage();
    testMsg1.add("LAST", 9.86);
    
    MutableFudgeMsg testMsg2 = OpenGammaFudgeContext.getInstance().newMessage();
    testMsg2.add("BID", 10.00);
    testMsg2.add("ASK", 10.05);
    
    _testMsgs = new MutableFudgeMsg[] { testMsg1, testMsg2 };
    
    Map<String, FudgeMsg> uniqueId2TestMsg = new HashMap<String, FudgeMsg>();
    uniqueId2TestMsg.put(TEST_LIVE_DATA_SPEC.getIdentifier(TEST_IDENTIFICATION_SCHEME), testMsg1);
    uniqueId2TestMsg.put(TEST_ID_1, testMsg1);
    uniqueId2TestMsg.put(TEST_ID_2, testMsg2);
    
    _server = new MockLiveDataServer(TEST_IDENTIFICATION_SCHEME, uniqueId2TestMsg);
    _client = LiveDataClientTestUtils.getInMemoryConduitClient(_server);
  }
  
  @AfterMethod
  public void closeClient() {
    _client.close();
  }

  @Test
  public void connectionToMarketDataApiDown() {
    // don't start server
    
    LiveDataSubscriptionResponse response = _client.snapshot(TEST_USER, TEST_LIVE_DATA_SPEC, 1000);
    assertNotNull(response);
    assertEquals(LiveDataSubscriptionResult.INTERNAL_ERROR, response.getSubscriptionResult());
    assertEquals("Connection to market data API down", response.getUserMessage());
  }
  
  @Test
  public void singleSnapshot() {
    _server.start();
    
    LiveDataSubscriptionResponse response = _client.snapshot(TEST_USER, TEST_LIVE_DATA_SPEC, 1000);
    assertNotNull(response);
    assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getSubscriptionResult());
    assertNotNull(response.getSnapshot());
    assertEquals(0, response.getSnapshot().getSequenceNumber());
    assertEquals(_testMsgs[0], response.getSnapshot().getFields());
  }
  
  @Test
  public void multipleSnapshots() {
    _server.start();
    
    LiveDataSpecification spec1 = new LiveDataSpecification(StandardRules.getNoNormalization().getId(), 
        ExternalId.of(TEST_IDENTIFICATION_SCHEME, TEST_ID_1));
    LiveDataSpecification spec2 = new LiveDataSpecification(StandardRules.getNoNormalization().getId(), 
        ExternalId.of(TEST_IDENTIFICATION_SCHEME, TEST_ID_2));
    
    Collection<LiveDataSubscriptionResponse> responses = _client.snapshot(TEST_USER, Sets.newHashSet(spec1, spec2), 1000);
    assertNotNull(responses);
    assertEquals(2, responses.size());
    for (LiveDataSubscriptionResponse response : responses) {

      assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getSubscriptionResult());
      assertNotNull(response.getSnapshot());

      if (response.getRequestedSpecification().equals(spec1)) {
        assertEquals(_testMsgs[0], response.getSnapshot().getFields());
      } else if (response.getRequestedSpecification().equals(spec2)) {
        assertEquals(_testMsgs[1], response.getSnapshot().getFields());
      } else {
        Assert.fail("Response for non-existent spec received");
      }
    }
  }
  
  @Test
  public void singleSubscribe() {
    _server.start();
    
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    
    _client.subscribe(TEST_USER, TEST_LIVE_DATA_SPEC, listener);
    
    assertEquals(1, listener.getSubscriptionResponses().size());
    assertEquals(LiveDataSubscriptionResult.SUCCESS, listener.getSubscriptionResponses().get(0).getSubscriptionResult());
    
    // As part of subscribe(), the client does a snapshot and sends it to the listener. 
    assertEquals(1, listener.getValueUpdates().size());
    assertEquals(_testMsgs[0], listener.getValueUpdates().get(0).getFields());
    
    _server.sendLiveDataToClient();

    // Normal data received.
    assertEquals(2, listener.getValueUpdates().size());
    assertEquals(_testMsgs[0], listener.getValueUpdates().get(1).getFields());
  }
  
  @Test
  public void multipleSubscribes() {
    _server.start();
    
    LiveDataSpecification spec1 = new LiveDataSpecification(StandardRules.getNoNormalization().getId(), 
        ExternalId.of(TEST_IDENTIFICATION_SCHEME, TEST_ID_1));
    LiveDataSpecification spec2 = new LiveDataSpecification(StandardRules.getNoNormalization().getId(), 
        ExternalId.of(TEST_IDENTIFICATION_SCHEME, TEST_ID_2));
    
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    
    _client.subscribe(TEST_USER, Sets.newHashSet(spec1, spec2), listener);
    
    assertEquals(2, listener.getSubscriptionResponses().size());
    assertEquals(LiveDataSubscriptionResult.SUCCESS, listener.getSubscriptionResponses().get(0).getSubscriptionResult());
    assertEquals(LiveDataSubscriptionResult.SUCCESS, listener.getSubscriptionResponses().get(1).getSubscriptionResult());
    
    // Snapshot x 2
    assertEquals(2, listener.getValueUpdates().size());
    
    _server.sendLiveDataToClient();
    
    // Snapshot x 2, subscription data x 2 = 4 overall
    assertEquals(4, listener.getValueUpdates().size());
    for (int i = 0; i < listener.getValueUpdates().size(); i++) {
      LiveDataValueUpdate update = listener.getValueUpdates().get(i);
      if (update.getSpecification().equals(spec1)) {
        assertEquals(_testMsgs[0], update.getFields());
      } else if (update.getSpecification().equals(spec2)) {
        assertEquals(_testMsgs[1], update.getFields());
      } else {
        Assert.fail("Response for non-existent spec received");
      }
    }
  }
  
  @Test
  public void subscribeUnsubscribeCycle() {
    _server.start();
    
    assertEquals(0, _server.getSubscriptions().size());
    
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    
    _client.subscribe(TEST_USER, TEST_LIVE_DATA_SPEC, listener);
    
    assertEquals(1, _server.getSubscriptions().size());
    
    _client.unsubscribe(TEST_USER, TEST_LIVE_DATA_SPEC, listener);
    
    // there would need to be a timeout before actual unsubscribe happens 
    assertEquals(1, _server.getSubscriptions().size());
    
    assertEquals(1, listener.getSubscriptionResponses().size()); 
    assertEquals(1, listener.getStoppedSubscriptions().size());
  }

}
