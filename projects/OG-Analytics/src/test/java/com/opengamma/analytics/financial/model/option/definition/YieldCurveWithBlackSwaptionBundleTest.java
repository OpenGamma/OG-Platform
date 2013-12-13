/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class YieldCurveWithBlackSwaptionBundleTest {

  private static final BlackFlatSwaptionParameters BLACK_SWAPTION = TestsDataSetsBlack.createBlackSwaptionEUR6();
  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesEUR();
  private static final YieldCurveWithBlackSwaptionBundle CURVES_WITH_BLACK = new YieldCurveWithBlackSwaptionBundle(BLACK_SWAPTION, CURVES);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBlack() {
    new YieldCurveWithBlackSwaptionBundle(null, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullurve() {
    new YieldCurveWithBlackSwaptionBundle(BLACK_SWAPTION, null);
  }

  @Test
  /**
   * Tests the object getters.
   */
  public void getter() {
    assertEquals("Yield curve with Black Swaption Surface: getter", BLACK_SWAPTION, CURVES_WITH_BLACK.getBlackParameters());
  }

  @Test
  /**
   * Tests the object equal and hash code methods.
   */
  public void equalHash() {
    assertTrue("Yield curve with Black Swaption Surface: equal and hash code", CURVES_WITH_BLACK.equals(CURVES_WITH_BLACK));
    final YieldCurveWithBlackSwaptionBundle other = new YieldCurveWithBlackSwaptionBundle(BLACK_SWAPTION, CURVES);
    assertTrue("Yield curve with Black Swaption Surface: equal and hash code", CURVES_WITH_BLACK.equals(other));
    assertEquals("Yield curve with Black Swaption Surface: equal and hash code", CURVES_WITH_BLACK.hashCode(), other.hashCode());
    YieldCurveWithBlackSwaptionBundle modified = new YieldCurveWithBlackSwaptionBundle(TestsDataSetsBlack.createBlackSwaptionEUR3(), CURVES);
    assertFalse("Yield curve with Black Swaption Surface: equal and hash code", CURVES_WITH_BLACK.equals(modified));
    modified = new YieldCurveWithBlackSwaptionBundle(BLACK_SWAPTION, TestsDataSetsBlack.createCurvesUSD());
    assertFalse("Yield curve with Black Swaption Surface: equal and hash code", CURVES_WITH_BLACK.equals(modified));
  }

}
