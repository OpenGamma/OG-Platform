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
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CurveNameMulticurveFilterTest {

  @Test
  public void bundle() {
    CurveNameMulticurveFilter usdDiscountingFilter =
        new CurveNameMulticurveFilter(MulticurveFilterTestUtils.USD_DISCOUNTING);
    Set<MulticurveMatchDetails> usdDiscountingMatches =
        usdDiscountingFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedUsdDiscountingMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_DISCOUNTING));
    assertEquals(expectedUsdDiscountingMatches, usdDiscountingMatches);

    CurveNameMulticurveFilter eurLiborFilter =
        new CurveNameMulticurveFilter(MulticurveFilterTestUtils.EUR_LIBOR_6M);
    Set<MulticurveMatchDetails> eurLiborMatches =
        eurLiborFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedEurLiborMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurLiborMatches, eurLiborMatches);

    CurveNameMulticurveFilter nonMatchingFilter =
        new CurveNameMulticurveFilter("unknown curve name");
    Set<MulticurveMatchDetails> nonMatchingMatches =
        nonMatchingFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), nonMatchingMatches);
  }

  @Test
  public void config() {
    MulticurveFilterTestUtils.initializeServiceContext();
    MulticurveId multicurveId = MulticurveId.of(MulticurveFilterTestUtils.CURVE_CONFIG_NAME);

    CurveNameMulticurveFilter usdDiscountingFilter =
        new CurveNameMulticurveFilter(MulticurveFilterTestUtils.USD_DISCOUNTING);
    Set<MulticurveMatchDetails> usdDiscountingMatches = usdDiscountingFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedUsdDiscountingMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_DISCOUNTING));
    assertEquals(expectedUsdDiscountingMatches, usdDiscountingMatches);

    CurveNameMulticurveFilter eurLiborFilter =
        new CurveNameMulticurveFilter(MulticurveFilterTestUtils.EUR_LIBOR_6M);
    Set<MulticurveMatchDetails> eurLiborMatches = eurLiborFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedEurLiborMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurLiborMatches, eurLiborMatches);

    CurveNameMulticurveFilter nonMatchingFilter =
        new CurveNameMulticurveFilter("unknown curve name");
    Set<MulticurveMatchDetails> nonMatchingMatches = nonMatchingFilter.apply(multicurveId);
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), nonMatchingMatches);
  }
}
