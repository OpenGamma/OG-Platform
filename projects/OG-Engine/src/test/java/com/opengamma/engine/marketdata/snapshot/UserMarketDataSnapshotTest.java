/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
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
import com.opengamma.engine.marketdata.ExternalIdBundleLookup;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class UserMarketDataSnapshotTest {

  @Test
  public void testQuerySnapshot() {
    StructuredMarketDataSnapshot snapshot = mock(StructuredMarketDataSnapshot.class);
    
    ExternalId testValueId = ExternalId.of("TestScheme", "Value1");

    UnstructuredMarketDataSnapshot globalValues = mock(UnstructuredMarketDataSnapshot.class); 
    when(globalValues.getValues()).thenReturn(generateGlobalValuesMap(testValueId, 234d));
    when(snapshot.getGlobalValues()).thenReturn(globalValues);
    
    UserMarketDataSnapshot userSnapshot = new UserMarketDataSnapshot(snapshot, new ExternalIdBundleLookup(null));
    
    UnstructuredMarketDataSnapshot yieldCurveValues = mock(UnstructuredMarketDataSnapshot.class);
    
    when(yieldCurveValues.getValues()).thenReturn(generateGlobalValuesMap(testValueId, 123d));
    YieldCurveSnapshot yieldCurveSnapshot = mock(YieldCurveSnapshot.class);
    when(yieldCurveSnapshot.getValues()).thenReturn(yieldCurveValues);

    Map<YieldCurveKey, YieldCurveSnapshot> yieldCurveMap = ImmutableMap.of(new YieldCurveKey(Currency.USD, "testCurve"), yieldCurveSnapshot);
    when(snapshot.getYieldCurves()).thenReturn(yieldCurveMap);
    
    ValueProperties yieldCurveConstraints = ValueProperties.with(ValuePropertyNames.CURVE, "testCurve").get();
    ValueRequirement yieldCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, ComputationTargetSpecification.of(Currency.USD), yieldCurveConstraints);
    SnapshotDataBundle yieldCurveBundle = (SnapshotDataBundle) userSnapshot.query(yieldCurveRequirement).getValue ();
    assertEquals(123d, yieldCurveBundle.getDataPoints().get(testValueId));
    
    ValueProperties unsatisfiableYieldCurveConstraints = ValueProperties
        .with(ValuePropertyNames.CURVE, "testCurve")
        .with("SomethingElse", "Value").get();
    ValueRequirement unsatisfiableYieldCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, ComputationTargetSpecification.of(Currency.USD),
        unsatisfiableYieldCurveConstraints);
    ComputedValue unsatisfiableYieldCurveBundle = userSnapshot.query(unsatisfiableYieldCurveRequirement);
    assertNull(unsatisfiableYieldCurveBundle);
  }

  private Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> generateGlobalValuesMap(ExternalId testValueId, Double marketValue) {
    MarketDataValueSpecification valueSpec = new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, testValueId);
    Map<String, ValueSnapshot> values = ImmutableMap.of(MarketDataRequirementNames.MARKET_VALUE, new ValueSnapshot(marketValue));
    return ImmutableMap.<MarketDataValueSpecification, Map<String, ValueSnapshot>>of(valueSpec, values);
  }
  
}
