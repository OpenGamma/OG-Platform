/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.google.common.collect.ImmutableSet;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.util.test.TestGroup;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

@Test(groups = TestGroup.UNIT)
public class AllNonDiscountCurvesFilterTest {

  @Test
  public void bundle() {
    AllNonDiscountCurvesFilter nonDiscountingFilter = new AllNonDiscountCurvesFilter();
    Set<MulticurveMatchDetails> nonDiscountingMatches =
        nonDiscountingFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedNonDiscountingMatches =
        ImmutableSet.of(
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_OVERNIGHT),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_OVERNIGHT),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_LIBOR_3M)
        );
    assertEquals(expectedNonDiscountingMatches, nonDiscountingMatches);
  }

  @Test
  public void config() {
    MulticurveFilterTestUtils.initializeServiceContext();
    MulticurveId multicurveId = MulticurveId.of(MulticurveFilterTestUtils.CURVE_CONFIG_NAME);

    AllNonDiscountCurvesFilter nonDiscountingFilter = new AllNonDiscountCurvesFilter();
    Set<MulticurveMatchDetails> nonDiscountingMatches = nonDiscountingFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedNonDiscountingMatches =
        ImmutableSet.of(
            // this is currently empty because the MulticurveMetadata currently does not
            // hold meta data of whether a curve is a discounting curve
        );
    assertEquals(expectedNonDiscountingMatches, nonDiscountingMatches);
  }

}
