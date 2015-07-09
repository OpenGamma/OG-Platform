/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class FxRateShiftTest {

  public void absolute() {
    FxRateShift shift = FxRateShift.absolute(0.2);
    CurrencyPairMatchDetails matchDetails = new CurrencyPairMatchDetails(false);
    assertEquals(1.4, shift.apply(1.2, matchDetails));
  }

  public void absoluteInverse() {
    FxRateShift shift = FxRateShift.absolute(0.2);
    CurrencyPairMatchDetails matchDetails = new CurrencyPairMatchDetails(true);
    // input rate is the reciprocal of 2.0 = 0.5
    // apply a shift of 0.2 gives 0.7
    // therefore expected shifted rate is 1 / 0.7
    assertEquals(1 / 0.7, shift.apply(2.0, matchDetails));
  }

  public void relative() {
    FxRateShift shift = FxRateShift.relative(0.2);
    CurrencyPairMatchDetails matchDetails = new CurrencyPairMatchDetails(false);
    assertEquals(1.2 * 1.2, shift.apply(1.2, matchDetails));
  }

  public void relativeInverse() {
    FxRateShift shift = FxRateShift.relative(0.1);
    CurrencyPairMatchDetails matchDetails = new CurrencyPairMatchDetails(true);
    // input rate is the reciprocal of 2.0 = 0.5
    // apply a shift of 0.1 gives (0.5 * 1.1) = 0.55
    // therefore expected shifted rate is 1 / 0.55
    assertEquals(1 / 0.55, shift.apply(2.0, matchDetails));
  }
}
