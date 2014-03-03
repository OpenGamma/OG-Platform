/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.test.TestLiveDataClient;
import com.opengamma.util.test.TestGroup;

/**
 * Test LiveDataSnapshotProvider.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryLKVLiveMarketDataProviderTest {

  private static final String _marketDataRequirement = MarketDataRequirementNames.MARKET_VALUE;

  protected ExternalId getTicker(final String ticker) {
    return ExternalId.of("Foo", ticker);
  }

  protected ValueRequirement constructRequirement(final String ticker) {
    return new ValueRequirement(_marketDataRequirement, ComputationTargetType.PRIMITIVE, getTicker(ticker));
  }

  protected ComputationTargetSpecification constructTargetSpec(final String ticker) {
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Bar", ticker + " UID"));
  }

  protected ValueSpecification constructSpecification(final String ticker) {
    return new ValueSpecification(_marketDataRequirement, constructTargetSpec(ticker), ValueProperties.with(ValuePropertyNames.FUNCTION, "MarketData").get());
  }

  @Test
  public void testSnapshotting() {
    final TestLiveDataClient liveDataClient = new TestLiveDataClient();
    try {
      FixedMarketDataAvailabilityProvider availabilityProvider = new FixedMarketDataAvailabilityProvider();
      availabilityProvider.addAvailableData(getTicker("test1"), constructSpecification("test1"));
      availabilityProvider.addAvailableData(getTicker("test2"), constructSpecification("test2"));
      availabilityProvider.addAvailableData(getTicker("test3"), constructSpecification("test3"));
      InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, availabilityProvider.getAvailabilityFilter(), UserPrincipal.getTestUser());
      ValueSpecification test1Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test1"), getTicker("test1"), constructRequirement("test1"));
      ValueSpecification test2Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test2"), getTicker("test2"), constructRequirement("test2"));
      ValueSpecification test3Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test3"), getTicker("test3"), constructRequirement("test3"));
      LiveDataSpecification test1test2FullyQualifiedSpecification = new LiveDataSpecification(liveDataClient.getDefaultNormalizationRuleSetId(), ExternalId.of("fq-test1test2", "test1test2"));
      LiveDataSpecification test3FullyQualifiedSpecification = new LiveDataSpecification(liveDataClient.getDefaultNormalizationRuleSetId(), ExternalId.of("fq-test3", "test3"));
      
      provider.subscribe(test1Specification);
      provider.subscribe(test2Specification);
      provider.subscribe(test3Specification);
      provider.subscribe(test3Specification);
      provider.subscribe(test3Specification);
      
      assertEquals(3, provider.getRequestedLiveDataSubscriptionCount());
      assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
      assertEquals(3, liveDataClient.getSubscriptionRequests().size());
      
      LiveDataSubscriptionResponse test1Response = new LiveDataSubscriptionResponse(LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test1Specification), LiveDataSubscriptionResult.SUCCESS, null, test1test2FullyQualifiedSpecification, "test1test2", null);
      LiveDataSubscriptionResponse test2Response = new LiveDataSubscriptionResponse(LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test2Specification), LiveDataSubscriptionResult.SUCCESS, null, test1test2FullyQualifiedSpecification, "test1test2", null);
      LiveDataSubscriptionResponse test3Response = new LiveDataSubscriptionResponse(LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test3Specification), LiveDataSubscriptionResult.SUCCESS, null, test3FullyQualifiedSpecification, "test3", null);
      
      provider.subscriptionResultsReceived(ImmutableList.of(test1Response, test2Response, test3Response));
      
      liveDataClient.subscriptionRequestSatisfied(
          Iterables.getOnlyElement(liveDataClient.getSubscriptionRequests().get(0)),
          test1Response);
      liveDataClient.subscriptionRequestSatisfied(
          Iterables.getOnlyElement(liveDataClient.getSubscriptionRequests().get(1)),
          test2Response);
      liveDataClient.subscriptionRequestSatisfied(
          Iterables.getOnlyElement(liveDataClient.getSubscriptionRequests().get(2)),
          test3Response);
      
      assertEquals(3, provider.getRequestedLiveDataSubscriptionCount());
      assertEquals(5, provider.getActiveValueSpecificationSubscriptionCount());
      assertEquals(3, liveDataClient.getSubscriptionRequests().size());
      
      final MutableFudgeMsg msg1 = new FudgeContext().newMessage();
      msg1.add(_marketDataRequirement, 52.07);
      final MutableFudgeMsg msg3a = new FudgeContext().newMessage();
      msg3a.add(_marketDataRequirement, 52.16);
      final MutableFudgeMsg msg3b = new FudgeContext().newMessage();
      msg3b.add(_marketDataRequirement, 52.17);
      liveDataClient.marketDataReceived(test1test2FullyQualifiedSpecification, msg1);
      liveDataClient.marketDataReceived(test3FullyQualifiedSpecification, msg3a);
      liveDataClient.marketDataReceived(test3FullyQualifiedSpecification, msg3b);

      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test1Specification));
      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test2Specification));
      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test3Specification));

      final MarketDataSnapshot snapshot = provider.snapshot(null);
      snapshot.init(Collections.<ValueSpecification>emptySet(), 0, TimeUnit.MILLISECONDS);
      final Double test1Value = (Double) snapshot.query(test1Specification);
      assertNotNull(test1Value);
      assertEquals(52.07, test1Value, 0.000001);
      final Double test2Value = (Double) snapshot.query(test2Specification);
      assertNotNull(test2Value);
      assertEquals(52.07, test2Value, 0.000001);
      final Double test3Value = (Double) snapshot.query(test3Specification);
      assertNotNull(test3Value);
      assertEquals(52.17, test3Value, 0.000001);
      assertNull(snapshot.query(constructSpecification("invalidticker")));
      
      provider.unsubscribe(test1Specification);
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test1Specification));
      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test2Specification));
      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test3Specification));
      assertEquals(0, liveDataClient.getCancelRequests().size());
      
      provider.unsubscribe(test2Specification);
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test1Specification));
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test2Specification));
      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test3Specification));
      assertEquals(1, liveDataClient.getCancelRequests().size());
      
      provider.unsubscribe(test3Specification);
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test1Specification));
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test2Specification));
      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test3Specification));
      assertEquals(1, liveDataClient.getCancelRequests().size());
      
      provider.unsubscribe(test3Specification);
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test1Specification));
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test2Specification));
      assertTrue(provider.getUnderlyingProvider().getAllValueKeys().contains(test3Specification));
      assertEquals(1, liveDataClient.getCancelRequests().size());
      
      provider.unsubscribe(test3Specification);
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test1Specification));
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test2Specification));
      assertFalse(provider.getUnderlyingProvider().getAllValueKeys().contains(test3Specification));
      assertEquals(2, liveDataClient.getCancelRequests().size());
    } finally {
      liveDataClient.close();
    }
  }
  
  @Test
  public void testFullyQualifiedSpecSameAsRequestSpec() {
    // Tests rewrite for PLAT-5947
    final TestLiveDataClient liveDataClient = new TestLiveDataClient();
    try {
      FixedMarketDataAvailabilityProvider availabilityProvider = new FixedMarketDataAvailabilityProvider();
      availabilityProvider.addAvailableData(getTicker("test1"), constructSpecification("test1"));
      availabilityProvider.addAvailableData(getTicker("test2"), constructSpecification("test2"));
      InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, availabilityProvider.getAvailabilityFilter(), UserPrincipal.getTestUser());
      ValueSpecification test1Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test1"), getTicker("test1"), constructRequirement("test1"));
      ValueSpecification test2Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test2"), getTicker("test2"), constructRequirement("test2"));
      
      // Make the fully-qualified specification the same as one of the requested specifications
      LiveDataSpecification test1test2FullyQualifiedSpecification = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test1Specification);
      
      provider.subscribe(test1Specification);
      provider.subscribe(test2Specification);
      
      assertEquals(2, provider.getRequestedLiveDataSubscriptionCount());
      assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
      assertEquals(2, liveDataClient.getSubscriptionRequests().size());
      
      LiveDataSubscriptionResponse test1Response = new LiveDataSubscriptionResponse(LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test1Specification), LiveDataSubscriptionResult.SUCCESS, null, test1test2FullyQualifiedSpecification, "test1test2", null);
      LiveDataSubscriptionResponse test2Response = new LiveDataSubscriptionResponse(LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test2Specification), LiveDataSubscriptionResult.SUCCESS, null, test1test2FullyQualifiedSpecification, "test1test2", null);

      provider.subscriptionResultsReceived(ImmutableList.of(test1Response, test2Response));
      
      liveDataClient.subscriptionRequestSatisfied(
          Iterables.getOnlyElement(liveDataClient.getSubscriptionRequests().get(0)),
          test1Response);
      liveDataClient.subscriptionRequestSatisfied(
          Iterables.getOnlyElement(liveDataClient.getSubscriptionRequests().get(1)),
          test2Response);

      assertEquals(2, provider.getRequestedLiveDataSubscriptionCount());
      assertEquals(2, provider.getActiveValueSpecificationSubscriptionCount());
      assertEquals(2, liveDataClient.getSubscriptionRequests().size());
      
      final MutableFudgeMsg msg1 = new FudgeContext().newMessage();
      msg1.add(_marketDataRequirement, 52.07);
      liveDataClient.marketDataReceived(test1test2FullyQualifiedSpecification, msg1);
      final MarketDataSnapshot snapshot = provider.snapshot(null);
      snapshot.init(Collections.<ValueSpecification>emptySet(), 0, TimeUnit.MILLISECONDS);
      final Double test1Value = (Double) snapshot.query(test1Specification);
      assertNotNull(test1Value);
      assertEquals(52.07, test1Value, 0.000001);
      final Double test2Value = (Double) snapshot.query(test2Specification);
      assertNotNull(test2Value);
      assertEquals(52.07, test2Value, 0.000001);
    } finally {
      liveDataClient.close();
    }
  }
  
  @Test
  public void testUnsubscribeWhileNewSubscriptionPending() {
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());    
    ValueSpecification primitiveSpecification = createPrimitiveValueSpec("AAPL.");
    
    provider.subscribe(primitiveSpecification);

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    
    provider.unsubscribe(primitiveSpecification);
    
    assertEquals(0, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    assertEquals(0, liveDataClient.getCancelRequests().size());
    
    LiveDataSpecification liveDataSpecification = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(primitiveSpecification);
    LiveDataSubscriptionResponse primitiveResponse = new LiveDataSubscriptionResponse(liveDataSpecification, LiveDataSubscriptionResult.SUCCESS, null, liveDataSpecification, "primitive", null);
    
    provider.subscriptionResultReceived(primitiveResponse);
    
    assertEquals(0, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    assertEquals(1, liveDataClient.getCancelRequests().size());
  }
  
  @Test
  public void testUnsubscribeWhileNewSubscriptionPendingWithExistingActiveSubscriptionOnSameFullyQualifiedSpecification() {
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());    
    ValueSpecification test1Specification = createPrimitiveValueSpec("AAPL.");
    
    provider.subscribe(test1Specification);

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    
    LiveDataSpecification test1LiveDataSpecification = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test1Specification);
    LiveDataSubscriptionResponse test1Response = new LiveDataSubscriptionResponse(test1LiveDataSpecification, LiveDataSubscriptionResult.SUCCESS, null, test1LiveDataSpecification, "primitive", null);
    provider.subscriptionResultReceived(test1Response);
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    
    ValueSpecification test2Specification = createPrimitiveValueSpec("VOD.");
    
    provider.subscribe(test2Specification);
    
    assertEquals(2, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(2, liveDataClient.getSubscriptionRequests().size());
    
    provider.unsubscribe(test2Specification);
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(2, liveDataClient.getSubscriptionRequests().size());
    
    // Simulate the test2 subscription request now receiving a response indicating the same fully-qualified specification as test1
    LiveDataSpecification test2LiveDataSpecification = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(test2Specification);
    LiveDataSubscriptionResponse test2Response = new LiveDataSubscriptionResponse(test2LiveDataSpecification, LiveDataSubscriptionResult.SUCCESS, null, test1LiveDataSpecification, "primitive", null);
    provider.subscriptionResultReceived(test2Response);    
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(2, liveDataClient.getSubscriptionRequests().size());
    
    // We already have existing demand for the fully-qualified spec, so we shouldn't cancel the subscription
    assertEquals(0, liveDataClient.getCancelRequests().size());
  }
  
  @Test
  public void testDuplicateSubscriptionResponseReceived() {
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());    
    ValueSpecification primitiveSpecification = createPrimitiveValueSpec("AAPL.");
    
    provider.subscribe(primitiveSpecification);

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    
    LiveDataSpecification liveDataSpecification = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(primitiveSpecification);
    LiveDataSubscriptionResponse primitiveResponse = new LiveDataSubscriptionResponse(liveDataSpecification, LiveDataSubscriptionResult.SUCCESS, null, liveDataSpecification, "primitive", null);
    
    provider.subscriptionResultReceived(primitiveResponse);
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    assertEquals(0, liveDataClient.getCancelRequests().size());
    
    provider.subscriptionResultReceived(primitiveResponse);
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    assertEquals(0, liveDataClient.getCancelRequests().size());
  }
  
  @Test
  public void testResubscribe() {
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());    
    ValueSpecification primitiveSpecification = createPrimitiveValueSpec("AAPL.");
    
    provider.subscribe(primitiveSpecification);

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    
    LiveDataSpecification liveDataSpecification = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(primitiveSpecification);
    LiveDataSubscriptionResponse primitiveResponse = new LiveDataSubscriptionResponse(liveDataSpecification, LiveDataSubscriptionResult.SUCCESS, null, liveDataSpecification, "primitive", null);
    
    provider.subscriptionResultReceived(primitiveResponse);
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
    assertEquals(0, liveDataClient.getCancelRequests().size());
    
    provider.resubscribe(ImmutableSet.of(liveDataSpecification.getIdentifiers().getExternalIds().first().getScheme()));
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(2, liveDataClient.getSubscriptionRequests().size());
    assertEquals(0, liveDataClient.getCancelRequests().size());
    
    provider.subscriptionResultReceived(primitiveResponse);
    
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(1, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(2, liveDataClient.getSubscriptionRequests().size());
    assertEquals(0, liveDataClient.getCancelRequests().size());
  }

  @Test
  public void testDifferentSpecsSameLiveData1() {
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());    
    ValueSpecification primitive = createPrimitiveValueSpec("AAPL.");
    ValueSpecification sec = createSecurityValueSpec("AAPL.");

    provider.subscribe(primitive);

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());

    provider.subscribe(sec);

    // Already have a pending request for this LiveDataSpecification
    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
  }

  @Test
  public void testDifferentSpecsSameLiveData2() {
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());
    ValueSpecification primitive = createPrimitiveValueSpec("AAPL.");
    ValueSpecification sec = createSecurityValueSpec("AAPL.");

    provider.subscribe(ImmutableSet.of(primitive, sec));

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());
  }

  @Test
  public void testSubscriptionHasTotalSubscriberCount() {
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(liveDataClient, mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());
    ValueSpecification primitive = createPrimitiveValueSpec("AAPL.");
    ValueSpecification sec = createSecurityValueSpec("AAPL.");

    provider.subscribe(primitive);

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());

    provider.subscribe(primitive);
    provider.subscribe(sec);

    assertEquals(1, provider.getRequestedLiveDataSubscriptionCount());
    assertEquals(0, provider.getActiveValueSpecificationSubscriptionCount());
    assertEquals(1, liveDataClient.getSubscriptionRequests().size());

    SubscriptionInfo sub = Iterables.getFirst(provider.queryByTicker("AAPL.").values(), null);
    assertEquals("PENDING", sub.getState());
    assertEquals(3, sub.getSubscriberCount());
  }

  private ValueSpecification createPrimitiveValueSpec(String ticker) {

    // Create spec of the form
    // VSpec[Market_All, CTSpec[PRIMITIVE, ExternalId-ACTIVFEED_TICKER~AAPL.], {Normalization=[OpenGamma],Function=[LiveMarketData],Id=[ACTIVFEED_TICKER~AAPL.]}]
    ExternalId externalId = ExternalSchemes.activFeedTickerSecurityId(ticker);

    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "LiveMarketData").with("Normalization", "OpenGamma").with("Id", externalId.toString()).get();

    ComputationTargetSpecification targetSpecification = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("ExternalId", externalId.toString()));

    return new ValueSpecification("Market_All", targetSpecification, properties);
  }

  private ValueSpecification createSecurityValueSpec(String ticker) {

    // Create spec of the form
    // VSpec[Market_All, CTSpec[SECURITY, DbSec~295921~0], {Normalization=[OpenGamma],Function=[LiveMarketData],Id=[ACTIVFEED_TICKER~AAPL.]}]
    ExternalId externalId = ExternalSchemes.activFeedTickerSecurityId(ticker);

    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "LiveMarketData").with("Normalization", "OpenGamma").with("Id", externalId.toString()).get();

    ComputationTargetSpecification targetSpecification = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("DbSec", "1234", "1"));

    return new ValueSpecification("Market_All", targetSpecification, properties);
  }
  
}
