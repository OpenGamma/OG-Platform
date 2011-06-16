/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateExtrapolationParameter;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
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
   * The standard day count 30/360 used in the data set. 
   */
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Expiry is between 0 and 10 years, maturity between 0 and 10 years. 
   * Beta is 0.5.  Alpha 0.05 at 1Y and 0.06 at 10Y. Rho 0.50 at 1Y and 0.30 at 10Y. Nu -0.25 at 1Y and 0.00 at 10Y. 
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR,
        LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1,
        1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR,
        LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, DAY_COUNT, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1() {
    return createSABR1(new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction, double shift) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift,
        0.05 + shift, 0.05 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1,
        1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR,
        LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, DAY_COUNT, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    double shift = 0.0001;
    return createSABR1AlphaBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(double shift) {
    return createSABR1AlphaBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped() {
    return createSABR1AlphaBumped(new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Rho data is bumped by the shift with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction, double shift) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR,
        LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift,
        -0.25 + shift, -0.25 + shift, -0.25 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1,
        1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR,
        LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, DAY_COUNT, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Rho data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    double shift = 0.0001;
    return createSABR1RhoBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(double shift) {
    return createSABR1RhoBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped() {
    double shift = 0.0001;
    return createSABR1RhoBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction, double shift) {
    InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR,
        LINEAR));
    VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1,
        1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift,
        0.50 + shift, 0.50 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, DAY_COUNT, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    double shift = 0.0001;
    return createSABR1NuBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Nu data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(double shift) {
    return createSABR1NuBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped() {
    double shift = 0.0001;
    return createSABR1NuBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a SABR surface with extrapolation parameters based on createSABR1.
   * @param sabrFunction The SABR function.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   * @return The SABR surface with extrapolation parameters.
   */
  public static SABRInterestRateExtrapolationParameter createSABRExtrapolation1(VolatilityFunctionProvider<SABRFormulaData> sabrFunction, double cutOffStrike, double mu) {
    SABRInterestRateParameters sabr = createSABR1();
    return SABRInterestRateExtrapolationParameter.from(sabr, cutOffStrike, mu);
  }

  /**
   * Create a SABR surface with extrapolation and with Hagan volatility function parameters based on createSABR1.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   * @return The SABR surface with extrapolation parameters.
   */
  public static SABRInterestRateExtrapolationParameter createSABRExtrapolation1(double cutOffStrike, double mu) {
    return createSABRExtrapolation1(new SABRHaganVolatilityFunction(), cutOffStrike, mu);
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

  /**
   * Create a yield curve bundle with tthree curves. One called "Credit" with a constant rate of 5%, one called "Discounting" with a constant rate of 4%, 
   * and one called "Forward" with a constant rate of 4.5%.
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesBond1() {
    final String CREDIT_CURVE_NAME = "Credit";
    final String DISCOUNTING_CURVE_NAME = "Repo";
    final String FORWARD_CURVE_NAME = "Forward";
    final YieldAndDiscountCurve CURVE_5 = new YieldCurve(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_4 = new YieldCurve(ConstantDoublesCurve.from(0.04));
    final YieldAndDiscountCurve CURVE_45 = new YieldCurve(ConstantDoublesCurve.from(0.045));
    YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(CREDIT_CURVE_NAME, CURVE_5);
    curves.setCurve(DISCOUNTING_CURVE_NAME, CURVE_4);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_45);
    return curves;
  }

}
