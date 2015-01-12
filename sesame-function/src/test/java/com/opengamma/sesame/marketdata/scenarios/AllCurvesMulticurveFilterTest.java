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
public class AllCurvesMulticurveFilterTest {

  @Test
  public void bundle() {
    AllCurvesMulticurveFilter filter = AllCurvesMulticurveFilter.INSTANCE;
    Set<MulticurveMatchDetails> matches = filter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    assertEquals(MulticurveFilterTestUtils.CURVE_NAMES, curveNames(matches));
  }

  @Test
  public void config() {
    MulticurveFilterTestUtils.initializeServiceContext();
    MulticurveId multicurveId = MulticurveId.of(MulticurveFilterTestUtils.CURVE_CONFIG_NAME);
    AllCurvesMulticurveFilter filter = AllCurvesMulticurveFilter.INSTANCE;
    Set<MulticurveMatchDetails> matches = filter.apply(multicurveId);
    assertEquals(MulticurveFilterTestUtils.CURVE_NAMES, curveNames(matches));
  }

  // TODO Java 8 - use stream().map()
  private Set<String> curveNames(Set<MulticurveMatchDetails> matches) {
    ImmutableSet.Builder<String> curveNamesBuilder = ImmutableSet.builder();

    for (MulticurveMatchDetails match : matches) {
      curveNamesBuilder.add(match.getCurveName());
    }
    return curveNamesBuilder.build();
  }
}
