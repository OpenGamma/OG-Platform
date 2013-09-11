/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.server.ExpirationManager;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.livedata.server.SubscriptionListener;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.livedata.test.LiveDataClientTestUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class CombiningBloombergLiveDataServerTest {

  private static final UserPrincipal TEST_USER = UserPrincipal.getTestUser();

  private CombiningBloombergLiveDataServer _server;
  private JmsLiveDataClient _liveDataClient;
  private BloombergReferenceDataProvider _underlying;
  private UnitTestingReferenceDataProvider _unitTestingProvider;

  @BeforeMethod
  public void setUpClass() {
    _underlying = BloombergLiveDataServerUtils.getUnderlyingProvider();
    _unitTestingProvider = new UnitTestingReferenceDataProvider(_underlying);
    _server = BloombergLiveDataServerUtils.startTestServer(
      CombiningBloombergLiveDataServerTest.class,
      new UnionFakeSubscriptionSelector(
        new BySchemeFakeSubscriptionSelector(ExternalSchemes.BLOOMBERG_BUID_WEAK, ExternalSchemes.BLOOMBERG_TICKER_WEAK),
        new ByTypeFakeSubscriptionSelector("SWAPTION VOLATILITY")),
      _unitTestingProvider);
    _liveDataClient = LiveDataClientTestUtils.getJmsClient(_server);
    _unitTestingProvider.reset();
  }

  @AfterMethod
  public void tearDownClass() {
    BloombergLiveDataServerUtils.stopTestServer(_server);
    _liveDataClient.stop();
    _underlying.stop();
  }

  //-------------------------------------------------------------------------
  @Test
  public void testFakeSubscribe() throws Exception {
    ExternalId broken = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "CZPFGQFC Curncy");
    ExternalId working = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "USPFJD5W Curncy");
    ExternalId workingStrong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USPFJD5W Curncy");
    
    List<ExternalId> instruments = Lists.newArrayList(broken, working, workingStrong);
    int repeats = 2;
    CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size() * repeats, 1 * repeats);
    for (int i = 0; i < repeats; i++) {
      subscribe(_liveDataClient, listener, instruments);
      unsubscribe(_liveDataClient, listener, instruments);
      _unitTestingProvider.rejectAllfurtherRequests();
    }
    
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    
    LiveDataSubscriptionResponse workingSub = null;
    LiveDataSubscriptionResponse workingStrongSub = null;
    for (LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(working)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        workingSub = response;
      } else if (response.getRequestedSpecification().getIdentifiers().contains(broken)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
      } else if (response.getRequestedSpecification().getIdentifiers().contains(workingStrong)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        workingStrongSub = response;
      } else {
        throw new Exception("Unexpected subscription response");
      }
    }
    assertEquals(workingSub.getFullyQualifiedSpecification(), workingStrongSub.getFullyQualifiedSpecification());
    assertEquals(workingSub.getTickDistributionSpecification(), workingStrongSub.getTickDistributionSpecification());
    List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    List<LiveDataValueUpdate> workingUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, working));
    assertEquals(allUpdates, workingUpdates);
    assertFalse(_unitTestingProvider.hadToRejectRequests()); // Necessary, since exceptions are expected from the live data service
  }

  //-------------------------------------------------------------------------
  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testRealSubscribe() throws Exception {
    ExternalId strong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");
    
    List<ExternalId> instruments = Lists.newArrayList(strong);
    CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);
    
    subscribe(_liveDataClient, listener, instruments);
    assertTrue(listener.waitUntilEnoughUpdatesReceived(60000));
    unsubscribe(_liveDataClient, listener, instruments);
    for (LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(strong)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
      }
    }
    
    List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    List<LiveDataValueUpdate> stronUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, strong));
    assertEquals(allUpdates, stronUpdates);
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testMixedSubscribe() throws Exception {
    ExternalId strong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");
    ExternalId weak = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "GBP Curncy");
    
    List<ExternalId> instruments = Lists.newArrayList(strong, weak);
    CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);
    
    subscribe(_liveDataClient, listener, instruments);
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    unsubscribe(_liveDataClient, listener, instruments);
    
    LiveDataSubscriptionResponse strongSub = null;
    LiveDataSubscriptionResponse weakSub = null;
    for (LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(strong)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        strongSub = response;
      } else if (response.getRequestedSpecification().getIdentifiers().contains(weak)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        weakSub = response;
      } else {
        throw new Exception("Unexpected subscription response");
      }
    }
    assertFalse(strongSub.getFullyQualifiedSpecification().equals(weakSub.getFullyQualifiedSpecification()));
    assertFalse(strongSub.getTickDistributionSpecification().equals(weakSub.getTickDistributionSpecification()));
    
    List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    List<LiveDataValueUpdate> stronUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, strong));
    List<LiveDataValueUpdate> weakUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, weak));
    assertEquals(1, weakUpdates.size());
    assertEquals(allUpdates.size(), weakUpdates.size() + stronUpdates.size());
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testBrokenSubscribe() throws Exception {
    ExternalId broken = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USSV15F Curncy");//Broken
    ExternalId working = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");
    
    List<ExternalId> instruments = Lists.newArrayList(broken, working);
    CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);
    
    subscribe(_liveDataClient, listener, instruments);
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    unsubscribe(_liveDataClient, listener, instruments);
    
    LiveDataSubscriptionResponse strongSub = null;
    LiveDataSubscriptionResponse weakSub = null;
    for (LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(working)) {
        assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getSubscriptionResult());
        strongSub = response;
      } else if (response.getRequestedSpecification().getIdentifiers().contains(broken)) {
        assertEquals(LiveDataSubscriptionResult.NOT_PRESENT, response.getSubscriptionResult());
        weakSub = response;
      } else {
        throw new Exception("Unexpected subscription response");
      }
    }
    assertFalse(strongSub.getFullyQualifiedSpecification().equals(weakSub.getFullyQualifiedSpecification()));
    assertFalse(strongSub.getTickDistributionSpecification().equals(weakSub.getTickDistributionSpecification()));
    
    List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    List<LiveDataValueUpdate> brokenUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, broken));
    List<LiveDataValueUpdate> workingUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, working));
    assertEquals(0, brokenUpdates.size());
    assertEquals(allUpdates.size(), brokenUpdates.size() + workingUpdates.size());
  }

  @Test
  public void testExpiration() throws Exception {
    int period = 15000;
    ExpirationManager expirationManager = _server.getExpirationManager();
    expirationManager.stop();
    expirationManager.setCheckPeriod(15000);
    expirationManager.setTimeoutExtension(15000);
    expirationManager.start();
    
    final AtomicInteger combinedSubs = countSubscriptions(_server);
    final AtomicInteger fakeSubs = countSubscriptions(_server.getFakeServer());
    final AtomicInteger realSubs = countSubscriptions(_server.getRealServer());
    
    ExternalId weak = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "GBP Curncy");
    
    List<ExternalId> instruments = Lists.newArrayList(weak);
    CollectingLiveDataListener listener = new CollectingLiveDataListener(1, 1);
    
    subscribe(_liveDataClient, listener, instruments);
    
    assertEquals(1, combinedSubs.get());
    assertEquals(combinedSubs.get(), fakeSubs.get());
    assertEquals(0, realSubs.get());
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    for (int i = 0; i < 3; i++) {
      expirationManager.extendPublicationTimeout(ImmutableSet.of(getLiveDataSpec(_liveDataClient, weak)));
      Thread.sleep(period / 2);
    }
    assertEquals(1, combinedSubs.get());
    assertEquals(combinedSubs.get(), fakeSubs.get());
    assertEquals(0, realSubs.get());
    
    unsubscribe(_liveDataClient, listener, instruments);
    
    Thread.sleep(period * 2);
    assertEquals(0, combinedSubs.get());
    assertEquals(combinedSubs.get(), fakeSubs.get());
    assertEquals(0, realSubs.get());
  }

  private AtomicInteger countSubscriptions(StandardLiveDataServer server) {
    final AtomicInteger fakeSubs = new AtomicInteger(0);
    server.addSubscriptionListener(new SubscriptionListener() {
      @Override
      public void unsubscribed(Subscription subscription) {
        fakeSubs.decrementAndGet();
      }
      @Override
      public void subscribed(Subscription subscription) {
        fakeSubs.incrementAndGet();
      }
    });
    return fakeSubs;
  }

  private void subscribe(LiveDataClient liveDataClient, LiveDataListener listener, Collection<ExternalId> instruments) {
    Collection<LiveDataSpecification> specs = getLiveDataSpecs(liveDataClient, instruments);
    liveDataClient.subscribe(TEST_USER, specs, listener);
  }

  private void unsubscribe(LiveDataClient liveDataClient, LiveDataListener listener, Collection<ExternalId> instruments) {
    Collection<LiveDataSpecification> specs = getLiveDataSpecs(liveDataClient, instruments);
    liveDataClient.unsubscribe(TEST_USER, specs, listener);
  }

  private Collection<LiveDataSpecification> getLiveDataSpecs(LiveDataClient liveDataClient,
      Collection<ExternalId> instruments) {
    Collection<LiveDataSpecification> specs = new ArrayList<LiveDataSpecification>(instruments.size());
    for (ExternalId instrument : instruments) {
      specs.add(getLiveDataSpec(liveDataClient, instrument));
    }
    return specs;
  }

  private LiveDataSpecification getLiveDataSpec(LiveDataClient liveDataClient, ExternalId id) {
    LiveDataSpecification requestedSpecification = new LiveDataSpecification(
        liveDataClient.getDefaultNormalizationRuleSetId(), id);
    return requestedSpecification;
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testRepeatedSubscriptions_BBG_80() throws Exception {
    ExternalId broken = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USSV15F Curncy");//Broken
    ExternalId working = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");
    ExternalId workingWeak = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "USPFJD5W Curncy");
    ExternalId workingStrong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USPFJD5W Curncy");
    
    List<ExternalId> instruments = Lists.newArrayList(broken, working, workingWeak, workingStrong);
    for (int i = 0; i < 10; i++) {
      CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);

      subscribe(_liveDataClient, listener, instruments);
      assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
      unsubscribe(_liveDataClient, listener, instruments);
      _unitTestingProvider.rejectAllfurtherRequests();
    }
    assertFalse(_unitTestingProvider.hadToRejectRequests());
  }

  //-------------------------------------------------------------------------
  public static class UnitTestingReferenceDataProvider extends AbstractReferenceDataProvider {
    private final ReferenceDataProvider _underlying;
    private java.util.concurrent.atomic.AtomicBoolean _locked = new java.util.concurrent.atomic.AtomicBoolean();
    private java.util.concurrent.atomic.AtomicBoolean _broken = new java.util.concurrent.atomic.AtomicBoolean();

    public UnitTestingReferenceDataProvider(ReferenceDataProvider underlying) {
      _underlying = underlying;
    }

    public void reset() {
      _locked.set(false);
      _broken.set(false);
    }

    public void rejectAllfurtherRequests() {
      _locked.set(true);
    }

    public boolean hadToRejectRequests() {
      return _broken.get();
    }

    @Override
    protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
      if (_locked.get()) {
        _broken.set(true);
      }
      assertFalse(_locked.get());
      return _underlying.getReferenceData(request);
    }
  }

}
