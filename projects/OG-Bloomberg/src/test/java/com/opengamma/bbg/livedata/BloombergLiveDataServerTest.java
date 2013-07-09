/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.livedata.test.LiveDataClientTestUtils;
import com.opengamma.livedata.test.StandardRulesUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergLiveDataServerTest {

  private final static UserPrincipal TEST_USER = UserPrincipal.getTestUser(); 

  private BloombergLiveDataServer _server;
  private JmsLiveDataClient _liveDataClient;

  @BeforeClass
  public void setUpClass() {
    _server = BloombergLiveDataServerUtils.startTestServer(BloombergLiveDataServerTest.class);
    _liveDataClient = LiveDataClientTestUtils.getJmsClient(_server);
  }

  @AfterClass
  public void tearDownClass() {
    BloombergLiveDataServerUtils.stopTestServer(_server);
    _liveDataClient.stop();
  }

  //-------------------------------------------------------------------------
  @Test
  public void testSnapshot() {
    LiveDataSubscriptionResponse snapshotResponse = snapshot("AAPL US Equity");
    
    assertNotNull(snapshotResponse);
    assertEquals(LiveDataSubscriptionResult.SUCCESS, snapshotResponse.getSubscriptionResult());
    StandardRulesUtils.validateOpenGammaMsg(snapshotResponse.getSnapshot().getFields());
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testSnapshotNotPresent() {
    LiveDataSubscriptionResponse snapshotResponse = snapshot("AAPL.O");
    assertNotNull(snapshotResponse);
    assertEquals(LiveDataSubscriptionResult.NOT_PRESENT, snapshotResponse.getSubscriptionResult());
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testSubscribeLimit() throws Exception {
    _server.setSubscriptionLimit(0);
    CollectingLiveDataListener listener = new CollectingLiveDataListener(1, 0);
    subscribe(_liveDataClient, listener, "AAPL US Equity");
    assertTrue(listener.waitUntilEnoughUpdatesReceived(1000));
    Thread.sleep(100000);
    for (LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
      assertTrue(response.getUserMessage().toLowerCase().contains("limit"));
    }
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testSubscribe() throws Exception {
    CollectingLiveDataListener listener = new CollectingLiveDataListener(5, 5);

    subscribe(_liveDataClient, listener, "USSW5 Curncy");
    subscribe(_liveDataClient, listener, "AAPL US Equity");
    subscribe(_liveDataClient, listener, "GBP Curncy");
    
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    
    for (LiveDataValueUpdate update : listener.getValueUpdates()) {
      assertEquals(1, update.getSpecification().getIdentifiers().size());
      assertNotNull(update.getSpecification().getIdentifier(ExternalSchemes.BLOOMBERG_BUID));
      assertNotNull(StandardRules.getOpenGammaRuleSetId(), update.getSpecification().getNormalizationRuleSetId());
    
      StandardRulesUtils.validateOpenGammaMsg(update.getFields());
    }
  }

  private void subscribe(LiveDataClient liveDataClient, LiveDataListener listener, String ticker) {
    LiveDataSpecification requestedSpecification = new LiveDataSpecification(
        liveDataClient.getDefaultNormalizationRuleSetId(),
        ExternalSchemes.bloombergTickerSecurityId(ticker));
    liveDataClient.subscribe(TEST_USER, requestedSpecification, listener);
  }
  
  private LiveDataSubscriptionResponse snapshot(String ticker) {
    LiveDataSpecification requestedSpecification = new LiveDataSpecification(
        _liveDataClient.getDefaultNormalizationRuleSetId(),
        ExternalSchemes.bloombergTickerSecurityId(ticker));
    return _liveDataClient.snapshot(TEST_USER, requestedSpecification, 3000);
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testPersistentServer() {
    // just check the Spring config's OK
    new ClassPathXmlApplicationContext("/com/opengamma/bbg/livedata/bbg-livedata-context.xml");
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void swapStripSubscriptions() throws Exception {
    CollectingLiveDataListener oneWeekListener = new CollectingLiveDataListener();
    CollectingLiveDataListener twoWeekListener = new CollectingLiveDataListener();
    CollectingLiveDataListener threeMonthListener = new CollectingLiveDataListener();
    
    subscribe(_liveDataClient, oneWeekListener, "US0001W Index");
    subscribe(_liveDataClient, twoWeekListener, "US0002W Index");
    subscribe(_liveDataClient, threeMonthListener, "US0003M Index");
    
    Thread.sleep(5000l);
    
    assertSwapCollectionValid("US0001W Index", oneWeekListener);
    assertSwapCollectionValid("US0002W Index", twoWeekListener);
    assertSwapCollectionValid("US0003M Index", threeMonthListener);
  }

  protected void assertSwapCollectionValid(String subscription, CollectingLiveDataListener listener) throws Exception {
    try {
      assertEquals(subscription, 1, listener.getSubscriptionResponses().size());
      LiveDataSubscriptionResponse subscriptionResponse = listener.getSubscriptionResponses().get(0);
      assertNotNull(subscriptionResponse);
      assertEquals(LiveDataSubscriptionResult.SUCCESS, subscriptionResponse.getSubscriptionResult());
      
      assertFalse(listener.getValueUpdates().isEmpty());
      for(LiveDataValueUpdate valueUpdate : listener.getValueUpdates()) {
        assertNotNull(valueUpdate);
        Set<String> fieldNames = valueUpdate.getFields().getAllFieldNames();
        boolean hasMarketValue = fieldNames.contains(MarketDataRequirementNames.MARKET_VALUE);
        assertTrue("Subscription " + subscription + " had field names " + fieldNames, hasMarketValue);
      }
      
    } catch(Exception e) {
      System.err.println("Didn't get valid response on subscription " + subscription);
      throw e;
    }
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void optionSnapshot() {
    String option = BloombergTestUtils.getSampleEquityOptionTicker();
    LiveDataSubscriptionResponse snapshotResponse = snapshot(option);
    
    assertNotNull(snapshotResponse);
    assertEquals(LiveDataSubscriptionResult.SUCCESS, snapshotResponse.getSubscriptionResult());
    StandardRulesUtils.validateOpenGammaMsg(snapshotResponse.getSnapshot().getFields());
    
    // especially outside market hours, it seems it's not guaranteed that you'll get implied vol
    // assertNotNull(snapshotResponse.getSnapshot().getFields().getDouble(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD));
  }

}
