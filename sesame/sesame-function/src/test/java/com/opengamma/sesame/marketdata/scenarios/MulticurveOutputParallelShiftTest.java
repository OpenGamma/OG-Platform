/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MulticurveOutputParallelShiftTest {

  private static final String CURVE_NAME = "curveName";
  private static final double RATE = 1.5;

  @Test
  public void absolute() {
    double shiftAmount = 0.01;
    MulticurveOutputParallelShift shift = MulticurveOutputParallelShift.absolute(shiftAmount);
    MulticurveBundle shiftedBundle = shift.apply(bundle(), StandardMatchDetails.multicurve(CURVE_NAME));
    YieldAndDiscountCurve shiftedCurve = shiftedBundle.getMulticurveProvider().getDiscountingCurves().get(Currency.USD);
    assertEquals(RATE + shiftAmount, shiftedCurve.getInterestRate(0d));
    assertEquals(RATE + shiftAmount, shiftedCurve.getInterestRate(1d));
  }
  @Test
  public void relative() {
    double shiftAmount = 0.01;
    MulticurveOutputParallelShift shift = MulticurveOutputParallelShift.relative(shiftAmount);
    MulticurveBundle shiftedBundle = shift.apply(bundle(), StandardMatchDetails.multicurve(CURVE_NAME));
    YieldAndDiscountCurve shiftedCurve = shiftedBundle.getMulticurveProvider().getDiscountingCurves().get(Currency.USD);
    assertEquals(RATE * (1 + shiftAmount), shiftedCurve.getInterestRate(0d));
    assertEquals(RATE * (1 + shiftAmount), shiftedCurve.getInterestRate(1d));
  }

  private MulticurveBundle bundle() {
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    ConstantDoublesCurve curve = new ConstantDoublesCurve(RATE, CURVE_NAME);
    YieldCurve yieldCurve = YieldCurve.from(curve);
    multicurve.setCurve(Currency.USD, yieldCurve);
    return new MulticurveBundle(multicurve, new CurveBuildingBlockBundle());
  }
}
