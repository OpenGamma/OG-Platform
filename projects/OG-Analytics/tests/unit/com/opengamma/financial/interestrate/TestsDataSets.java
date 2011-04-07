/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
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
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Expiry is between 0 and 10 years, maturity between 1 and 10 years. 
   * Beta is 0.5.  Alpha 0.05 at 1Y and 0.06 at 10Y. Rho 0.50 at 1Y and 0.30 at 10Y. Nu -0.25 at 1Y and 0.00 at 10Y. 
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameter(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1() {
    return createSABR1(new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1AlphaBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction, double shift) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift},
        new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameter(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1AlphaBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    double shift = 0.0001;
    return createSABR1AlphaBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1AlphaBumped(double shift) {
    return createSABR1AlphaBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1AlphaBumped() {
    return createSABR1AlphaBumped(new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1RhoBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {-0.2499, -0.2499, -0.2499, -0.2499, -0.2499, -0.2499, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameter(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Expiry is between 0 and 5 years, maturity between 1 and 5. Beta is 0.5. 
   * Alpha 0.05 at 1Y and 0.06 at 5Y. Rho 0.50 at 1Y and 0.30 at 5Y. Nu -0.25 at 1Y and 0.00 at 5Y. 
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1RhoBumped() {
    return createSABR1RhoBumped(new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1NuBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10},
        new double[] {0.5001, 0.5001, 0.5001, 0.5001, 0.5001, 0.5001, 0.3001, 0.3001, 0.3001, 0.3001, 0.3001, 0.3001}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameter(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Expiry is between 0 and 5 years, maturity between 1 and 5. Beta is 0.5. 
   * Alpha 0.05 at 1Y and 0.06 at 5Y. Rho 0.50 at 1Y and 0.30 at 5Y. Nu -0.25 at 1Y and 0.00 at 5Y. 
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameter createSABR1NuBumped() {
    return createSABR1NuBumped(new SABRHaganVolatilityFunction());
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
