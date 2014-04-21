/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link InMemoryLKVMarketDataProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryLKVMarketDataProviderTest {

  public void testPermissions() {
    final InMemoryLKVMarketDataProvider provider = new InMemoryLKVMarketDataProvider();
    final MarketDataPermissionProvider permissions = provider.getPermissionProvider();
    assertTrue(permissions instanceof PermissiveMarketDataPermissionProvider);
  }

  public void testCompatible() {
    final InMemoryLKVMarketDataProvider provider = new InMemoryLKVMarketDataProvider();
    assertTrue(provider.isCompatible(LiveMarketDataSpecification.LIVE_SPEC));
  }

  public void testAddRemoveValues() {
    final InMemoryLKVMarketDataProvider provider = new InMemoryLKVMarketDataProvider();
    assertTrue(provider.getAllValueKeys().isEmpty());
    provider.addValue(new ValueRequirement("Foo", ComputationTargetSpecification.NULL), "FooValue");
    assertEquals(provider.getAllValueKeys().size(), 1);
    provider.addValue(new ValueRequirement("Bar", ComputationTargetSpecification.NULL), "BarValue");
    assertEquals(provider.getAllValueKeys().size(), 2);
    final ExternalIdBundle identifiers = ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("B", "1"));
    provider.addValue(new ValueRequirement("Foo", ComputationTargetRequirement.of(identifiers)), "FooValue");
    provider.addValue(new ValueRequirement("Bar", ComputationTargetRequirement.of(identifiers)), "BarValue");
    assertEquals(provider.getAllValueKeys().size(), 4);
    provider.removeValue(new ValueRequirement("Foo", ComputationTargetSpecification.NULL));
    assertEquals(provider.getAllValueKeys().size(), 3);
    provider.removeValue(new ValueRequirement("Bar", ComputationTargetSpecification.NULL));
    assertEquals(provider.getAllValueKeys().size(), 2);
  }

  public void testAvailabilityAndSnapshot() {
    final InMemoryLKVMarketDataProvider provider = new InMemoryLKVMarketDataProvider();
    ValueSpecification fooNull = provider.getAvailabilityProvider(MarketData.live()).getAvailability(ComputationTargetSpecification.NULL, null,
        new ValueRequirement("Foo", ComputationTargetSpecification.NULL));
    assertNull(fooNull);
    provider.addValue(new ValueRequirement("Foo", ComputationTargetSpecification.NULL), "FooValue1");
    fooNull = provider.getAvailabilityProvider(MarketData.live()).getAvailability(ComputationTargetSpecification.NULL, null, new ValueRequirement("Foo", ComputationTargetSpecification.NULL));
    assertNotNull(fooNull);
    provider.addValue(new ValueRequirement("Bar", ComputationTargetSpecification.NULL), "BarValue1");
    final ValueSpecification barNull = provider.getAvailabilityProvider(MarketData.live()).getAvailability(ComputationTargetSpecification.NULL, null,
        new ValueRequirement("Bar", ComputationTargetSpecification.NULL));
    assertNotNull(barNull);
    final ExternalIdBundle identifiers = ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("B", "1"));
    provider.addValue(new ValueRequirement("Foo", ComputationTargetRequirement.of(identifiers)), "FooValue2");
    final ValueSpecification foo = provider.getAvailabilityProvider(MarketData.live()).getAvailability(ComputationTargetSpecification.of(UniqueId.of("X", "1")), identifiers,
        new ValueRequirement("Foo", ComputationTargetRequirement.of(identifiers)));
    assertNotNull(foo);
    assertEquals(provider.getAllValueKeys(), ImmutableSet.of(fooNull, barNull, foo));
    assertEquals(provider.getCurrentValue(fooNull), "FooValue1");
    assertEquals(provider.getCurrentValue(barNull), "BarValue1");
    assertEquals(provider.getCurrentValue(foo), "FooValue2");
    MarketDataSnapshot snapshot = provider.snapshot(MarketData.live());
    snapshot.init();
    provider.addValue(fooNull, "FooValue3");
    assertEquals(provider.getCurrentValue(fooNull), "FooValue3");
    assertEquals(snapshot.query(fooNull), "FooValue1");
    assertEquals(snapshot.query(barNull), "BarValue1");
    assertEquals(snapshot.query(foo), "FooValue2");
    snapshot = provider.snapshot(MarketData.live());
    snapshot.init();
    assertEquals(snapshot.query(fooNull), "FooValue3");
  }

}
