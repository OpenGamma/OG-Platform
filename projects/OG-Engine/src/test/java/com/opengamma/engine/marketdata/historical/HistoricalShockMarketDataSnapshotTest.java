/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class HistoricalShockMarketDataSnapshotTest {

  private static final ValueSpecification SPEC1 =
      new ValueSpecification("valueName1",
                             new ComputationTargetSpecification(ComputationTargetType.CURRENCY, UniqueId.of("id", "1")),
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "function1").get());
  private static final ValueSpecification SPEC2 =
      new ValueSpecification("valueName2",
                             new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, UniqueId.of("id", "2")),
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "function2").get());
  private static final ValueSpecification SPEC3 =
      new ValueSpecification("valueName3",
                             new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("id", "3")),
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "function3").get());

  @Test
  public void proportionalShock() {
    HistoricalShockMarketDataSnapshot snapshot =
        new HistoricalShockMarketDataSnapshot(HistoricalShockMarketDataSnapshot.ShockType.PROPORTIONAL,
                                              historicalSnapshot1(),
                                              historicalSnapshot2(),
                                              baseSnapshot());
    assertEquals(11d, snapshot.query(SPEC1));
    assertEquals(22d, snapshot.query(SPEC2));
    assertEquals(44d, snapshot.query(SPEC3));
    assertEquals(ImmutableMap.<ValueSpecification, Object>of(SPEC1, 11d, SPEC2, 22d, SPEC3, 44d),
                 snapshot.query(ImmutableSet.of(SPEC1, SPEC2, SPEC3)));
  }

  @Test
  public void absoluteShock() {
    HistoricalShockMarketDataSnapshot snapshot =
        new HistoricalShockMarketDataSnapshot(HistoricalShockMarketDataSnapshot.ShockType.ABSOLUTE,
                                              historicalSnapshot1(),
                                              historicalSnapshot2(),
                                              baseSnapshot());
    assertEquals(10.1d, snapshot.query(SPEC1));
    assertEquals(20.2d, snapshot.query(SPEC2));
    assertEquals(40.4d, snapshot.query(SPEC3));
    assertEquals(ImmutableMap.<ValueSpecification, Object>of(SPEC1, 10.1d, SPEC2, 20.2d, SPEC3, 40.4d),
                 snapshot.query(ImmutableSet.of(SPEC1, SPEC2, SPEC3)));
  }

  private static MarketDataSnapshot historicalSnapshot1() {
    MarketDataSnapshot snapshot = mock(MarketDataSnapshot.class);
    when(snapshot.query(SPEC1)).thenReturn(1d);
    when(snapshot.query(SPEC2)).thenReturn(2d);
    when(snapshot.query(SPEC3)).thenReturn(4d);
    return snapshot;
  }

  private static MarketDataSnapshot historicalSnapshot2() {
    MarketDataSnapshot snapshot = mock(MarketDataSnapshot.class);
    when(snapshot.query(SPEC1)).thenReturn(1.1d);
    when(snapshot.query(SPEC2)).thenReturn(2.2d);
    when(snapshot.query(SPEC3)).thenReturn(4.4d);
    return snapshot;
  }

  private static MarketDataSnapshot baseSnapshot() {
    MarketDataSnapshot snapshot = mock(MarketDataSnapshot.class);
    when(snapshot.query(SPEC1)).thenReturn(10d);
    when(snapshot.query(SPEC2)).thenReturn(20d);
    when(snapshot.query(SPEC3)).thenReturn(40d);
    return snapshot;
  }
}
