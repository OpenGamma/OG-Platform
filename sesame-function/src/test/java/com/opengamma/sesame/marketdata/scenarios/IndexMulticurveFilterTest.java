/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class IndexMulticurveFilterTest {

  @Test
  public void bundle() {
    IndexMulticurveFilter gbpOnFilter = new IndexMulticurveFilter(MulticurveFilterTestUtils.GBP_OVERNIGHT_INDEX);
    Set<MulticurveMatchDetails> gbpOnMatches =
        gbpOnFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedGpbOnMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_OVERNIGHT));
    assertEquals(expectedGpbOnMatches, gbpOnMatches);

    IndexMulticurveFilter eurLiborFilter = new IndexMulticurveFilter(MulticurveFilterTestUtils.EUR_LIBOR_6M_INDEX);
    Set<MulticurveMatchDetails> eurLiborMatches =
        eurLiborFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedEurLiborMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurLiborMatches, eurLiborMatches);

    IndexON chfOnIndex = new IndexON("CHF Overnight Index", Currency.CHF, DayCounts.ACT_360, 0);
    IndexMulticurveFilter nonMatchingFilter = new IndexMulticurveFilter(chfOnIndex);
    Set<MulticurveMatchDetails> nonMatchingMatches =
        nonMatchingFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), nonMatchingMatches);
  }

  @Test
  public void config() {
    MulticurveFilterTestUtils.initializeServiceContext();
    MulticurveId multicurveId = MulticurveId.of(MulticurveFilterTestUtils.CURVE_CONFIG_NAME);

    IndexMulticurveFilter gbpOnFilter = new IndexMulticurveFilter(MulticurveFilterTestUtils.GBP_OVERNIGHT_INDEX);
    Set<MulticurveMatchDetails> gbpOnMatches = gbpOnFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedGpbOnMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_OVERNIGHT));
    assertEquals(expectedGpbOnMatches, gbpOnMatches);

    IndexMulticurveFilter eurLiborFilter = new IndexMulticurveFilter(MulticurveFilterTestUtils.EUR_LIBOR_6M_INDEX);
    Set<MulticurveMatchDetails> eurLiborMatches = eurLiborFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedEurLiborMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurLiborMatches, eurLiborMatches);

    IndexON chfOnIndex = new IndexON("CHF Overnight Index", Currency.CHF, DayCounts.ACT_360, 0);
    IndexMulticurveFilter nonMatchingFilter = new IndexMulticurveFilter(chfOnIndex);
    Set<MulticurveMatchDetails> nonMatchingMatches = nonMatchingFilter.apply(multicurveId);
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), nonMatchingMatches);
  }
}
