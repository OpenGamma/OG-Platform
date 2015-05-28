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
public class AllDiscountCurvesFilterTest {

  @Test
  public void bundle() {
    AllDiscountCurvesFilter discountingFilter = new AllDiscountCurvesFilter();
    Set<MulticurveMatchDetails> discountingMatches =
        discountingFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedDiscountingMatches =
        ImmutableSet.of(
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_DISCOUNTING),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_DISCOUNTING)
        );
    assertEquals(expectedDiscountingMatches, discountingMatches);
  }

  @Test
  public void config() {
    MulticurveFilterTestUtils.initializeServiceContext();
    MulticurveId multicurveId = MulticurveId.of(MulticurveFilterTestUtils.CURVE_CONFIG_NAME);

    AllDiscountCurvesFilter discountingFilter = new AllDiscountCurvesFilter();
    Set<MulticurveMatchDetails> discountingMatches = discountingFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedDiscountingMatches =
        ImmutableSet.of(
            // this is currently empty because the MulticurveMetadata currently does not
            // hold meta data of whether a curve is a discounting curve
        );
    assertEquals(expectedDiscountingMatches, discountingMatches);
  }

}
