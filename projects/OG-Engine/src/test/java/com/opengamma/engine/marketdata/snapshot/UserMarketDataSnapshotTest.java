/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class UserMarketDataSnapshotTest {

  private Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> generateGlobalValuesMap(final ExternalId testValueId, final Double marketValue) {
    final MarketDataValueSpecification valueSpec = new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, testValueId);
    final Map<String, ValueSnapshot> values = ImmutableMap.of(MarketDataRequirementNames.MARKET_VALUE, new ValueSnapshot(marketValue));
    return ImmutableMap.<MarketDataValueSpecification, Map<String, ValueSnapshot>>of(valueSpec, values);
  }

  private UserMarketDataSnapshot createSnapshot() {
    final StructuredMarketDataSnapshot snapshot = mock(StructuredMarketDataSnapshot.class);
    final MarketDataSnapshotSource snapshotSource = mock(MarketDataSnapshotSource.class);
    final UniqueId snapshotId = UniqueId.of("TestSnapshot", "1");
    when(snapshotSource.get(snapshotId)).thenReturn(snapshot);
    final ExternalId testValueId = ExternalId.of("TestScheme", "Value1");
    final UnstructuredMarketDataSnapshot globalValues = mock(UnstructuredMarketDataSnapshot.class);
    when(globalValues.getValues()).thenReturn(generateGlobalValuesMap(testValueId, 234d));
    when(snapshot.getGlobalValues()).thenReturn(globalValues);
    final UserMarketDataSnapshot userSnapshot = new UserMarketDataSnapshot(snapshotSource, snapshotId);
    userSnapshot.init();
    final UnstructuredMarketDataSnapshot yieldCurveValues = mock(UnstructuredMarketDataSnapshot.class);
    when(yieldCurveValues.getValues()).thenReturn(generateGlobalValuesMap(testValueId, 123d));
    final YieldCurveSnapshot yieldCurveSnapshot = mock(YieldCurveSnapshot.class);
    when(yieldCurveSnapshot.getValues()).thenReturn(yieldCurveValues);
    final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurveMap = ImmutableMap.of(new YieldCurveKey(Currency.USD, "testCurve"), yieldCurveSnapshot);
    when(snapshot.getYieldCurves()).thenReturn(yieldCurveMap);
    return userSnapshot;
  }

  @Test
  public void testGetAvailabilityAndQuery() {
    final UserMarketDataSnapshot snapshot = createSnapshot();
    final MarketDataAvailabilityProvider provider = snapshot.getAvailabilityProvider();
    final ComputationTargetSpecification target = ComputationTargetSpecification.of(Currency.USD);
    ValueSpecification specification = provider.getAvailability(target, Currency.USD,
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, target, ValueProperties.with(ValuePropertyNames.CURVE, "testCurve").get()));
    assertNotNull(specification);
    assertNotNull(snapshot.query(specification));
    assertEquals(123d, ((SnapshotDataBundle) snapshot.query(specification)).getDataPoint(ExternalId.of("TestScheme", "Value1")));
    specification = provider.getAvailability(target, Currency.USD,
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, target, ValueProperties.with(ValuePropertyNames.CURVE, "testCurve").with("SomethingElse", "Value").get()));
    assertNull(specification);
    // TODO: Test the surface, cube and global values
  }

}
