/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * Sets of market data used in tests.
 */
public class TestsDataSetsBond {

  /**
   * The linear interpolator/ flat extrapolator. 
   */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final String DSC_EUR = "EUR Discounting";
  private static final String CURVE_GOVT_DE = "EUR - DE Govt";
  private static final String CURVE_GOVT_FR = "EUR - FR Govt";
  private static final String DSC_USD = "USD Discounting";
  private static final String CURVE_GOVT_US = "USD - US Govt";

  /**
   * Create a yield curve bundle with two curves.
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesBondEURDE() {
    final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    final YieldAndDiscountCurve CURVE_45 = YieldCurve.from(ConstantDoublesCurve.from(0.045));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DSC_EUR, CURVE_4);
    curves.setCurve(CURVE_GOVT_DE, CURVE_45);
    return curves;
  }

  public static String[] nameCurvesBondEURDE() {
    return new String[] {DSC_EUR, CURVE_GOVT_DE };
  }

  /**
   * Create a yield curve bundle with two curves.
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesBondEURFR() {
    final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    final YieldAndDiscountCurve CURVE_45 = YieldCurve.from(ConstantDoublesCurve.from(0.045));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DSC_EUR, CURVE_4);
    curves.setCurve(CURVE_GOVT_FR, CURVE_45);
    return curves;
  }

  public static String[] nameCurvesBondEURFR() {
    return new String[] {DSC_EUR, CURVE_GOVT_FR };
  }

  /**
   * Create a yield curve bundle with two curves.
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesBondUSDUS() {
    final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    final YieldAndDiscountCurve CURVE_45 = YieldCurve.from(ConstantDoublesCurve.from(0.045));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DSC_USD, CURVE_4);
    curves.setCurve(CURVE_GOVT_US, CURVE_45);
    return curves;
  }

  public static String[] nameCurvesBondUSDUS() {
    return new String[] {DSC_USD, CURVE_GOVT_US };
  }

}
