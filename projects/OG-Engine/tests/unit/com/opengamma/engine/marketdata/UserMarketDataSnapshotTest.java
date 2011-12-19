/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class UserMarketDataSnapshotTest {

  @Test
  public void testQuerySnapshot() {
    StructuredMarketDataSnapshot snapshot = mock(StructuredMarketDataSnapshot.class);

    MarketDataSnapshotSource snapshotSource = mock(MarketDataSnapshotSource.class);
    UniqueId snapshotId = UniqueId.of("TestSnapshot", "1");
    when(snapshotSource.getSnapshot(snapshotId)).thenReturn(snapshot);
    
    UserMarketDataSnapshot userSnapshot = new UserMarketDataSnapshot(snapshotSource, snapshotId);
    userSnapshot.init();
    
    UnstructuredMarketDataSnapshot yieldCurveValues = mock(UnstructuredMarketDataSnapshot.class);
    when(yieldCurveValues.getValues()).thenReturn(ImmutableMap.<MarketDataValueSpecification, Map<String, ValueSnapshot>>of());
    YieldCurveSnapshot yieldCurveSnapshot = mock(YieldCurveSnapshot.class);
    when(yieldCurveSnapshot.getValues()).thenReturn(yieldCurveValues);

    Map<YieldCurveKey, YieldCurveSnapshot> yieldCurveMap = ImmutableMap.of(new YieldCurveKey(Currency.USD, "testCurve"), yieldCurveSnapshot);
    when(snapshot.getYieldCurves()).thenReturn(yieldCurveMap);
    
    ValueProperties yieldCurveConstraints = ValueProperties.with(ValuePropertyNames.CURVE, "testCurve").get();
    ValueRequirement yieldCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, Currency.USD, yieldCurveConstraints);
    userSnapshot.query(yieldCurveRequirement);
    verify(yieldCurveSnapshot.getValues());
  }

}
