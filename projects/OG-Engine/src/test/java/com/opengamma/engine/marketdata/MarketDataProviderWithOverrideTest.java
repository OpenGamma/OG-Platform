/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * Tests {@link MarketDataProviderWithOverride}
 */
@Test
public class MarketDataProviderWithOverrideTest {

  public void testSubscriptionFailure() throws InterruptedException {
    final MockMarketDataProvider p1 = new MockMarketDataProvider("p1", true, 1);
    final MockMarketDataProvider p2 = new MockMarketDataProvider("p2", false, 1);
    final MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(p1, p2);
    final MarketDataListener listener = mock(MarketDataListener.class);
    provider.addListener(listener);

    final ValueSpecification spec = getSpecification(1);

    provider.subscribe(spec);
    p1.awaitSubscriptionResponses();
    p2.awaitSubscriptionResponses();

    verify(listener).subscriptionFailed(spec, "p2");
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionsSucceeded(Collections.singleton(Mockito.<ValueSpecification>anyObject()));
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionFailed(Mockito.<ValueSpecification>anyObject(), Mockito.anyString());
  }

  public void testSubscriptionSuccess() throws InterruptedException {
    final MockMarketDataProvider p1 = new MockMarketDataProvider("p1", true, 1);
    final MockMarketDataProvider p2 = new MockMarketDataProvider("p2", true, 1);
    final MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(p1, p2);
    final MarketDataListener listener = mock(MarketDataListener.class);
    provider.addListener(listener);

    final ValueSpecification spec = getSpecification(1);

    provider.subscribe(spec);
    p1.awaitSubscriptionResponses();
    p2.awaitSubscriptionResponses();

    verify(listener).subscriptionsSucceeded(new ArrayList<ValueSpecification>(Arrays.asList(spec)));
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionsSucceeded(Collections.singleton(Mockito.<ValueSpecification>anyObject()));
    verify(listener, VerificationModeFactory.noMoreInteractions()).subscriptionFailed(Mockito.<ValueSpecification>anyObject(), Mockito.anyString());

    p1.valuesChanged(Collections.singleton(spec));
    p2.valuesChanged(Collections.singleton(spec));
    verify(listener, VerificationModeFactory.times(2)).valuesChanged(Collections.singleton(spec));
  }

  public void testSnapshotNoOverrides() throws InterruptedException {
    final ValueSpecification spec1 = getSpecification(1);
    final ValueSpecification spec2 = getSpecification(2);

    final MockMarketDataProvider overrideProvider = new MockMarketDataProvider("p1", true, 1);
    overrideProvider.put(spec1, "value1");
    final MockMarketDataProvider underlyingProvider = new MockMarketDataProvider("p2", true, 1);
    underlyingProvider.put(spec2, "value2");
    final MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(underlyingProvider, overrideProvider);

    final MarketDataSnapshot snapshot = provider.snapshot(null);
    assertEquals(1, overrideProvider.getAndResetSnapshotCount());
    assertEquals(1, underlyingProvider.getAndResetSnapshotCount());

    assertEquals("value1", snapshot.query(spec1));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(0, underlyingProvider.getAndResetQueryCount());

    assertEquals("value2", snapshot.query(spec2));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(1, underlyingProvider.getAndResetQueryCount());

    final ValueSpecification spec3 = getSpecification(3);
    assertNull(snapshot.query(spec3));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(1, underlyingProvider.getAndResetQueryCount());

    assertEquals("value1", snapshot.query(spec1));
  }

  public void testSnapshotWithOverrides() throws InterruptedException {
    final ValueSpecification spec1 = getSpecification(1);

    final MockMarketDataProvider overrideProvider = new MockMarketDataProvider("p1", true, 1);
    overrideProvider.put(spec1, "value1");
    final MockMarketDataProvider underlyingProvider = new MockMarketDataProvider("p2", true, 1);
    underlyingProvider.put(spec1, "value2");
    final MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(underlyingProvider, overrideProvider);

    final MarketDataSnapshot snapshot = provider.snapshot(null);
    assertEquals(1, overrideProvider.getAndResetSnapshotCount());
    assertEquals(1, underlyingProvider.getAndResetSnapshotCount());

    // p1 should override the value in p2
    assertEquals("value1", snapshot.query(spec1));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(0, underlyingProvider.getAndResetQueryCount());
  }

  public void testSnapshotWithAlgorithmOverrides() throws InterruptedException {
    final ValueSpecification spec1 = getSpecification(1);
    final MockMarketDataProvider overrideProvider = new MockMarketDataProvider("p1", true, 1);
    overrideProvider.put(spec1, new OverrideOperation() {
      @Override
      public Object apply(final ValueRequirement requirement, final Object original) {
        assertEquals("Value-1", requirement.getValueName());
        assertEquals("value2", original);
        return "value1";
      }
    });
    final MockMarketDataProvider underlyingProvider = new MockMarketDataProvider("p2", true, 1);
    underlyingProvider.put(spec1, "value2");
    final MarketDataProviderWithOverride provider = new MarketDataProviderWithOverride(underlyingProvider, overrideProvider);
    final MarketDataSnapshot snapshot = provider.snapshot(null);
    assertEquals(1, overrideProvider.getAndResetSnapshotCount());
    assertEquals(1, underlyingProvider.getAndResetSnapshotCount());
    // p1 should override the value in p2, but p2 was queried for the operation
    assertEquals("value1", snapshot.query(spec1));
    assertEquals(1, overrideProvider.getAndResetQueryCount());
    assertEquals(1, underlyingProvider.getAndResetQueryCount());
  }

  private ValueSpecification getSpecification(final int number) {
    return new ValueSpecification("Value-" + number, ComputationTargetSpecification.of(UniqueId.of("Scheme", "Target")), ValueProperties.with(ValuePropertyNames.FUNCTION, "MarketData").get());
  }

}
