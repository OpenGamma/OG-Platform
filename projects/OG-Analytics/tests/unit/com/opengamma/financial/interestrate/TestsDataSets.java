/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

/**
 * Sets of market data used in tests.
 */
public class TestsDataSets {

  /**
   * Linear interpolator. Used for SABR parameters interpolation.
   */
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();

  /**
   * Create a set of SABR parameter surface (linearly interpolated). Expiry is between 0 and 5 years, maturity between 1 and 5. Beta is 0.5. 
   * Alpha 0.05 at 1Y and 0.06 at 5Y. Rho 0.50 at 1Y and 0.30 at 5Y. Nu -0.25 at 1Y and 0.00 at 5Y. 
   * @return The SABE parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1() {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5}, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5}, new double[] {0.05,
        0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5}, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5}, new double[] {0.5, 0.5,
        0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5}, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5}, new double[] {0.50, 0.50,
        0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5}, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5}, new double[] {-0.25,
        -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameter(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility);
  }

  /**
   * Create a yield curve bundle with two curves. One called "Funding" with a constant rate of 5% and one called "Forward" with a constant rate of 4%;
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurves1() {
    final String FUNDING_CURVE_NAME = "Funding";
    final String FORWARD_CURVE_NAME = "Forward";
    //  final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
    final YieldAndDiscountCurve CURVE_5 = new YieldCurve(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_4 = new YieldCurve(ConstantDoublesCurve.from(0.04));
    YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    return curves;
  }

}
