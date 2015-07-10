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
public class IndexNameMulticurveFilterTest {

  @Test
  public void bundle() {
    IndexNameMulticurveFilter gbpOnFilter =
        new IndexNameMulticurveFilter(MulticurveFilterTestUtils.GBP_OVERNIGHT_INDEX_NAME);
    Set<MulticurveMatchDetails> gbpOnMatches =
        gbpOnFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedGpbOnMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_OVERNIGHT));
    assertEquals(expectedGpbOnMatches, gbpOnMatches);

    IndexNameMulticurveFilter eurLiborFilter =
        new IndexNameMulticurveFilter(MulticurveFilterTestUtils.EUR_LIBOR_6M_INDEX_NAME);
    Set<MulticurveMatchDetails> eurLiborMatches =
        eurLiborFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    Set<MulticurveMatchDetails> expectedEurLiborMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurLiborMatches, eurLiborMatches);

    IndexNameMulticurveFilter nonMatchingFilter = new IndexNameMulticurveFilter("unknown index");
    Set<MulticurveMatchDetails> nonMatchingMatches =
        nonMatchingFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), nonMatchingMatches);
  }

  @Test
  public void config() {
    MulticurveFilterTestUtils.initializeServiceContext();
    MulticurveId multicurveId = MulticurveId.of(MulticurveFilterTestUtils.CURVE_CONFIG_NAME);

    IndexNameMulticurveFilter gbpOnFilter =
        new IndexNameMulticurveFilter(MulticurveFilterTestUtils.GBP_OVERNIGHT_INDEX_NAME);
    Set<MulticurveMatchDetails> gbpOnMatches = gbpOnFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedGpbOnMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_OVERNIGHT));
    assertEquals(expectedGpbOnMatches, gbpOnMatches);

    IndexNameMulticurveFilter eurLiborFilter =
        new IndexNameMulticurveFilter(MulticurveFilterTestUtils.EUR_LIBOR_6M_INDEX_NAME);
    Set<MulticurveMatchDetails> eurLiborMatches = eurLiborFilter.apply(multicurveId);
    Set<MulticurveMatchDetails> expectedEurLiborMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurLiborMatches, eurLiborMatches);

    IndexNameMulticurveFilter nonMatchingFilter = new IndexNameMulticurveFilter("unknown index");
    Set<MulticurveMatchDetails> nonMatchingMatches = nonMatchingFilter.apply(multicurveId);
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), nonMatchingMatches);
  }
}
