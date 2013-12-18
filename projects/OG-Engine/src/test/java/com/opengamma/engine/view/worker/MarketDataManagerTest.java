/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for the subscription tracking logic of the {@link MarketDataManager}.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataManagerTest {

  private MarketDataManager _manager;

  @BeforeMethod
  public void setUp() throws Exception {
    _manager = new MarketDataManager(createChangeListener(), createResolver(), null, null);
    
    List<MarketDataSpecification> spec = Lists.newArrayList();
    spec.add(LiveMarketDataSpecification.LIVE_SPEC);
    
    _manager.createSnapshotManagerForCycle(new UserPrincipal("bloggs", "127.0.0.1"), ImmutableList.copyOf(spec));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionWithNullListenerFails() {
    new MarketDataManager(null, createResolver(), null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionWithNullResolverFails() {
    new MarketDataManager(createChangeListener(), null, null, null);
  }

  @Test
  public void testQueryForNonExistentSub() {
    _manager.requestMarketDataSubscriptions(ImmutableSet.of(createValueSpecForMarketValue("AAPL.")));

    assertThat(_manager.querySubscriptionState("BOGUS").size(), is(0));
  }

  @Test
  public void testMarketDataRequestIsInitiallyPending() {
    _manager.requestMarketDataSubscriptions(ImmutableSet.of(createValueSpecForMarketValue("AAPL.")));

    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.PENDING);
  }

  @Test
  public void testMarketDataSubscriptionSucceeding() {
    ImmutableSet<ValueSpecification> valueSpecs = ImmutableSet.of(createValueSpecForMarketValue("AAPL."));
    _manager.requestMarketDataSubscriptions(valueSpecs);
    _manager.subscriptionsSucceeded(valueSpecs);

    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.ACTIVE);
  }

  @Test
  public void testMarketDataSubscriptionFailing() {
    ValueSpecification valueSpec = createValueSpecForMarketValue("AAPL.");
    _manager.requestMarketDataSubscriptions(ImmutableSet.of(valueSpec));
    _manager.subscriptionFailed(valueSpec, "Que?");

    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.FAILED);
  }

  @Test
  public void testMarketDataSubscriptionRemoval() {
    ImmutableSet<ValueSpecification> valueSpecs1 = ImmutableSet.of(createValueSpecForMarketValue("AAPL."));
    _manager.requestMarketDataSubscriptions(valueSpecs1);
    _manager.subscriptionsSucceeded(valueSpecs1);

    Set<ValueSpecification> valueSpecs2 = createMarketDataValueSpecs("GOOG.");
    _manager.requestMarketDataSubscriptions(valueSpecs2);
    _manager.subscriptionsSucceeded(valueSpecs2);

    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.REMOVED);
    checkSingleSubscriptionState("GOOG.", SubscriptionStateQuery.SubscriptionState.ACTIVE);
  }

  @Test
  public void testMarketDataLifecycle() {
    ValueSpecification spec = createValueSpecForMarketValue("AAPL.");
    ImmutableSet<ValueSpecification> valueSpecs = ImmutableSet.of(spec);

    _manager.requestMarketDataSubscriptions(valueSpecs);
    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.PENDING);

    _manager.subscriptionsSucceeded(valueSpecs);
    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.ACTIVE);

    _manager.requestMarketDataSubscriptions(ImmutableSet.<ValueSpecification>of());
    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.REMOVED);

    _manager.requestMarketDataSubscriptions(valueSpecs);
    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.PENDING);

    _manager.subscriptionFailed(spec, "Why?");
    checkSingleSubscriptionState("AAPL.", SubscriptionStateQuery.SubscriptionState.FAILED);

  }

  @Test
  public void testAllMatchingTickersAreReturned() {
    Set<ValueSpecification> valueSpecs =
        createMarketDataValueSpecs("AAPL.", "AAPL/G4NHG.O", "AAPL/G4G3F.", "GOOG.", "GOOG/GsG~K.");
    _manager.requestMarketDataSubscriptions(valueSpecs);

    assertThat(_manager.querySubscriptionState("AAPL").size(), is(3));
    assertThat(_manager.querySubscriptionState("AAPL.").size(), is(1));
    assertThat(_manager.querySubscriptionState("GOOG").size(), is(2));
    assertThat(_manager.querySubscriptionState("GOOG.").size(), is(1));
  }

  @Test
  public void testNullQueryResultsInAllMatchingTickersReturned() {
    Set<ValueSpecification> valueSpecs =
        createMarketDataValueSpecs("AAPL.", "AAPL/G4NHG.O", "AAPL/G4G3F.", "GOOG.", "GOOG/GsG~K.");
    _manager.requestMarketDataSubscriptions(valueSpecs);

    assertThat(_manager.querySubscriptionState(null).size(), is(5));
  }

  @Test
  public void testEmptyQueryResultsInAllMatchingTickersReturned() {
    Set<ValueSpecification> valueSpecs =
        createMarketDataValueSpecs("AAPL.", "AAPL/G4NHG.O", "AAPL/G4G3F.", "GOOG.", "GOOG/GsG~K.");
    _manager.requestMarketDataSubscriptions(valueSpecs);

    assertThat(_manager.querySubscriptionState("").size(), is(5));
  }

  @Test
  public void testQueryByType() {
    Set<ValueSpecification> appleSpec = createMarketDataValueSpecs("AAPL.");
    Set<ValueSpecification> appleOptions = createMarketDataValueSpecs("AAPL/G4NHG.O", "AAPL/G4G3F.");
    Set <ValueSpecification> googleOptions = createMarketDataValueSpecs("GOOG.", "GOOG/GsG~K.");

    _manager.requestMarketDataSubscriptions(
        ImmutableSet.<ValueSpecification>builder()
            .addAll(appleOptions)
            .addAll(appleSpec)
            .build());
    assertThat(_manager.getFailedSubscriptionCount(), is(0));
    assertThat(_manager.getPendingSubscriptionCount(), is(3));

    // Drop the AAPL request
    _manager.requestMarketDataSubscriptions(
        ImmutableSet.<ValueSpecification>builder()
            .addAll(appleOptions)
            .addAll(googleOptions)
            .build());

    _manager.subscriptionFailed(createValueSpecForMarketValue("AAPL/G4NHG.O"), "oops");
    _manager.subscriptionsSucceeded(createMarketDataValueSpecs("AAPL/G4G3F."));

    assertThat(_manager.getFailedSubscriptionCount(), is(1));
    assertThat(_manager.getPendingSubscriptionCount(), is(2));
    assertThat(_manager.getRemovedSubscriptionCount(), is(1));
    assertThat(_manager.getActiveSubscriptionCount(), is(1));
  }

  @Test
  public void testValueSpecsOnSameTickerAreDistinguishedByGet() {
    Set<ValueSpecification> specs = ImmutableSet.of(createValueSpecForMarketValue("AAPL."),
                                                    createValueSpecForDividendYield("AAPL."));
    _manager.requestMarketDataSubscriptions(specs);
    assertThat(_manager.getPendingSubscriptionCount(), is(2));
    Set<String> keys = _manager.queryPendingSubscriptions().keySet();
    assertThat(keys.size(), is(2));

    checkKeyMatches(keys);
  }

  @Test
  public void testValueSpecsOnSameTickerAreDistinguishedByQuery() {
    Set<ValueSpecification> specs = ImmutableSet.of(createValueSpecForMarketValue("AAPL."), createValueSpecForDividendYield("AAPL."));
    _manager.requestMarketDataSubscriptions(specs);
    Set<String> keys = _manager.querySubscriptionState("").keySet();
    assertThat(keys.size(), is(2));

    checkKeyMatches(keys);
  }

  @Test
  public void testUnexpectedSubscriptionNotificationsIgnored() {
    Set<ValueSpecification> specs1 = ImmutableSet.of(createValueSpecForMarketValue("AAPL."));
    _manager.subscriptionsSucceeded(specs1);
    
    assertThat(_manager.querySubscriptionState("AAPL.").size(), is(0));
    assertThat(_manager.getFailedSubscriptionCount(), is(0));
    assertThat(_manager.getPendingSubscriptionCount(), is(0));
    assertThat(_manager.getRemovedSubscriptionCount(), is(0));
    assertThat(_manager.getActiveSubscriptionCount(), is(0));
    
    ValueSpecification spec2 = createValueSpecForMarketValue("GOOG.");
    _manager.subscriptionFailed(spec2, "Not authorized");
    
    assertThat(_manager.querySubscriptionState("GOOG.").size(), is(0));
    assertThat(_manager.getFailedSubscriptionCount(), is(0));
    assertThat(_manager.getPendingSubscriptionCount(), is(0));
    assertThat(_manager.getRemovedSubscriptionCount(), is(0));
    assertThat(_manager.getActiveSubscriptionCount(), is(0));
  }
  
  @Test
  public void testUnexpectedChangeOfSubscriptionState() {
    ValueSpecification spec = createValueSpecForMarketValue("AAPL.");
    Set<ValueSpecification> specs = ImmutableSet.of(spec);
    _manager.requestMarketDataSubscriptions(specs);
    
    assertThat(_manager.getFailedSubscriptionCount(), is(0));
    assertThat(_manager.getPendingSubscriptionCount(), is(1));
    assertThat(_manager.getRemovedSubscriptionCount(), is(0));
    assertThat(_manager.getActiveSubscriptionCount(), is(0));
    
    _manager.subscriptionsSucceeded(specs);
    
    assertThat(_manager.getFailedSubscriptionCount(), is(0));
    assertThat(_manager.getPendingSubscriptionCount(), is(0));
    assertThat(_manager.getRemovedSubscriptionCount(), is(0));
    assertThat(_manager.getActiveSubscriptionCount(), is(1));
    
    _manager.subscriptionFailed(spec, "Not authorized");
    
    assertThat(_manager.getFailedSubscriptionCount(), is(1));
    assertThat(_manager.getPendingSubscriptionCount(), is(0));
    assertThat(_manager.getRemovedSubscriptionCount(), is(0));
    assertThat(_manager.getActiveSubscriptionCount(), is(0));
  }
  
  private void checkKeyMatches(Set<String> keys) {
    boolean mvMatch = false;
    boolean dyMatch = false;

    for (String key : keys) {
      assertThat(key.contains("AAPL"), is(true));
      mvMatch = mvMatch || key.contains("Market_Value");
      dyMatch = dyMatch || key.contains("Dividend_Yield");
    }

    assertThat(mvMatch, is(true));
    assertThat(dyMatch, is(true));
  }

  private void checkSingleSubscriptionState(String ticker, MarketDataManager.SubscriptionState expectedState) {

    Map<String, MarketDataManager.SubscriptionStatus> stateMap = _manager.querySubscriptionState(ticker);
    assertThat(Iterables.getOnlyElement(stateMap.values()).getState(), is(expectedState.name()));
  }

  private Set<ValueSpecification> createMarketDataValueSpecs(String... tickers) {

    ImmutableSet.Builder<ValueSpecification> builder = ImmutableSet.builder();
    for (String ticker : tickers) {
      builder.add(createValueSpecForMarketValue(ticker));
    }
    return builder.build();
  }

  private ValueSpecification createValueSpecForMarketValue(String ticker) {
    return createValueSpec(ticker, "Market_Value");
  }

  private ValueSpecification createValueSpecForDividendYield(String ticker) {
    return createValueSpec(ticker, "Dividend_Yield");
  }

  private ValueSpecification createValueSpec(String ticker, String valueName) {
    UniqueId uniqueId = UniqueId.of(ExternalSchemes.ACTIVFEED_TICKER.getName(), ticker);

    ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.FUNCTION, "MarketDataSourcingFunction")
        .get();

    ComputationTargetSpecification targetSpecification =
        new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uniqueId);

    return new ValueSpecification(valueName, targetSpecification, properties);
  }

  private MarketDataChangeListener createChangeListener() {
    return new MarketDataChangeListener() {
      @Override
      public void onMarketDataValuesChanged(Collection<ValueSpecification> valueSpecifications) { }
    };
  }

  private MarketDataProviderResolver createResolver() {
    return new MarketDataProviderResolver() {
      @Override
      public MarketDataProvider resolve(UserPrincipal marketDataUser, MarketDataSpecification snapshotSpec) {

        MarketDataProvider mock = mock(MarketDataProvider.class);
        when(mock.snapshot(any(MarketDataSpecification.class))).thenReturn(mock(CompositeMarketDataSnapshot.class));
        return mock;
      }
    };
  }
}
