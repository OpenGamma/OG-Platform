/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * Sets of market data used in Forex tests.
 */
public class ForexTestsDataSets {

  private static final String DISCOUNTING_EUR = "Discounting EUR";
  private static final String DISCOUNTING_USD = "Discounting USD";
  private static final String DISCOUNTING_GBP = "Discounting GBP";
  private static final String DISCOUNTING_KRW = "Discounting KRW";

  /**
   * Create a yield curve bundle with three curves. One called "Discounting EUR" with a constant rate of 2.50%, one called "Discounting USD" with a constant rate of 1.00%
   * and one called "Discounting GBP" with a constant rate of 2.00%; "Discounting KRW" with a constant rate of 3.21%; 
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesForex() {
    final YieldAndDiscountCurve CURVE_EUR = new YieldCurve(ConstantDoublesCurve.from(0.0250));
    final YieldAndDiscountCurve CURVE_USD = new YieldCurve(ConstantDoublesCurve.from(0.0100));
    final YieldAndDiscountCurve CURVE_GBP = new YieldCurve(ConstantDoublesCurve.from(0.0200));
    final YieldAndDiscountCurve CURVE_KRW = new YieldCurve(ConstantDoublesCurve.from(0.0321));
    YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DISCOUNTING_EUR, CURVE_EUR);
    curves.setCurve(DISCOUNTING_USD, CURVE_USD);
    curves.setCurve(DISCOUNTING_GBP, CURVE_GBP);
    curves.setCurve(DISCOUNTING_KRW, CURVE_KRW);
    return curves;
  }

  public static String[] curveNames() {
    return new String[] {DISCOUNTING_EUR, DISCOUNTING_USD, DISCOUNTING_GBP, DISCOUNTING_KRW};
  }

}
