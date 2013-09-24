/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

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
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
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

  public void snapshotting() {
    final TestLiveDataClient client = new TestLiveDataClient();
    final FixedMarketDataAvailabilityProvider availabilityProvider = new FixedMarketDataAvailabilityProvider();
    availabilityProvider.addAvailableData(getTicker("test1"), constructSpecification("test1"));
    availabilityProvider.addAvailableData(getTicker("test2"), constructSpecification("test2"));
    availabilityProvider.addAvailableData(getTicker("test3"), constructSpecification("test3"));
    final LiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(client, availabilityProvider.getAvailabilityFilter(), UserPrincipal.getTestUser());
    final ValueSpecification test1Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test1"), getTicker("test1"), constructRequirement("test1"));
    final ValueSpecification test2Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test2"), getTicker("test2"), constructRequirement("test2"));
    final ValueSpecification test3Specification = provider.getAvailabilityProvider(MarketData.live()).getAvailability(constructTargetSpec("test3"), getTicker("test3"), constructRequirement("test3"));
    provider.subscribe(test1Specification);
    provider.subscribe(test2Specification);
    provider.subscribe(test3Specification);
    provider.subscribe(test3Specification);
    provider.subscribe(test3Specification);
    final MutableFudgeMsg msg1 = new FudgeContext().newMessage();
    msg1.add(_marketDataRequirement, 52.07);
    final MutableFudgeMsg msg2 = new FudgeContext().newMessage();
    msg2.add(_marketDataRequirement, 52.15);
    final MutableFudgeMsg msg3a = new FudgeContext().newMessage();
    msg3a.add(_marketDataRequirement, 52.16);
    final MutableFudgeMsg msg3b = new FudgeContext().newMessage();
    msg3b.add(_marketDataRequirement, 52.17);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), getTicker("test1")), msg1);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), getTicker("test2")), msg2);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), getTicker("test3")), msg3a);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), getTicker("test3")), msg3b);
    final MarketDataSnapshot snapshot = provider.snapshot(null);
    snapshot.init(Collections.<ValueSpecification>emptySet(), 0, TimeUnit.MILLISECONDS);
    final Double test1Value = (Double) snapshot.query(test1Specification);
    assertNotNull(test1Value);
    assertEquals(52.07, test1Value, 0.000001);
    final Double test2Value = (Double) snapshot.query(test2Specification);
    assertNotNull(test2Value);
    assertEquals(52.15, test2Value, 0.000001);
    final Double test3Value = (Double) snapshot.query(test3Specification);
    assertNotNull(test3Value);
    assertEquals(52.17, test3Value, 0.000001);
    assertNull(snapshot.query(constructSpecification("invalidticker")));
  }

  @Test
  public void testDifferentSpecsSameLiveData1() {

    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(mock(LiveDataClient.class), mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());
    ValueSpecification primitive = createPrimitiveValueSpec("AAPL.");
    ValueSpecification sec = createSecurityValueSpec("AAPL.");

    provider.subscribe(primitive);

    assertThat(provider.getSubscriptionCount(), is(1));
    assertThat(provider.getSpecificationCount(), is(1));

    provider.subscribe(sec);

    assertThat(provider.getSpecificationCount(), is(2));
    assertThat(provider.getSubscriptionCount(), is(1));

  }

  @Test
  public void testDifferentSpecsSameLiveData2() {

    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(mock(LiveDataClient.class), mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());
    ValueSpecification primitive = createPrimitiveValueSpec("AAPL.");
    ValueSpecification sec = createSecurityValueSpec("AAPL.");

    provider.subscribe(ImmutableSet.of(primitive, sec));

    assertThat(provider.getSpecificationCount(), is(2));
    assertThat(provider.getSubscriptionCount(), is(1));

  }

  @Test
  public void testSubscriptionHasTotalSubscriberCount() {

    InMemoryLKVLiveMarketDataProvider provider = new InMemoryLKVLiveMarketDataProvider(mock(LiveDataClient.class), mock(MarketDataAvailabilityFilter.class), UserPrincipal.getTestUser());
    ValueSpecification primitive = createPrimitiveValueSpec("AAPL.");
    ValueSpecification sec = createSecurityValueSpec("AAPL.");

    provider.subscribe(primitive);

    assertThat(provider.getSubscriptionCount(), is(1));
    assertThat(provider.getSpecificationCount(), is(1));

    provider.subscribe(primitive);
    provider.subscribe(sec);

    assertThat(provider.getSpecificationCount(), is(2));
    assertThat(provider.getSubscriptionCount(), is(1));

    SubscriptionInfo sub = Iterables.getFirst(provider.queryByTicker("AAPL.").values(), null);
    assertThat(sub.getState(), is("PENDING"));
    assertThat(sub.getSubscriberCount(), is(3));
  }

  private ValueSpecification createPrimitiveValueSpec(String ticker) {

    // Create spec of the form
    // VSpec[Market_All, CTSpec[PRIMITIVE, ExternalId-ACTIVFEED_TICKER~AAPL.], {Normalization=[OpenGamma],Function=[LiveMarketData],Id=[ACTIVFEED_TICKER~AAPL.]}]
    ExternalId externalId = ExternalSchemes.activFeedTickerSecurityId(ticker);

    ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.FUNCTION, "LiveMarketData")
        .with("Normalization", "OpenGamma")
        .with("Id", externalId.toString())
        .get();

    ComputationTargetSpecification targetSpecification =
        new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE,
                                           UniqueId.of("ExternalId", externalId.toString()));

    return new ValueSpecification("Market_All", targetSpecification, properties);
  }

  private ValueSpecification createSecurityValueSpec(String ticker) {

    // Create spec of the form
    // VSpec[Market_All, CTSpec[SECURITY, DbSec~295921~0], {Normalization=[OpenGamma],Function=[LiveMarketData],Id=[ACTIVFEED_TICKER~AAPL.]}]
    ExternalId externalId = ExternalSchemes.activFeedTickerSecurityId(ticker);

    ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.FUNCTION, "LiveMarketData")
        .with("Normalization", "OpenGamma")
        .with("Id", externalId.toString())
        .get();

    ComputationTargetSpecification targetSpecification =
        new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("DbSec", "1234", "1"));

    return new ValueSpecification("Market_All", targetSpecification, properties);
  }

}
