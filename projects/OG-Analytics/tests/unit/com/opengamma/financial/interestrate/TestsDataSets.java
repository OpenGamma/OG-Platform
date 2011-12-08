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
import com.opengamma.financial.model.option.definition.SABRInterestRateExtrapolationParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
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
   * The linear interpolator/ flat extrapolator.  Used for SABR parameters interpolation.
   */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
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
  public static SABRInterestRateParameters createSABR1(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2,
        5, 10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
        0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(LINEAR, LINEAR));
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
        0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25,
        -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, new GridInterpolator2D(LINEAR, LINEAR));
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50,
        0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(LINEAR, LINEAR));
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, sabrFunction);
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
  public static SABRInterestRateParameters createSABR1AlphaBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift,
        0.05 + shift, 0.05 + shift, 0.05 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(
        LINEAR, LINEAR));
    //    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final double shift = 0.0001;
    return createSABR1AlphaBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1AlphaBumped(final double shift) {
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
  public static SABRInterestRateParameters createSABR1RhoBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(
        LINEAR, LINEAR));
    //    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift,
        -0.25 + shift, -0.25 + shift, -0.25 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, new GridInterpolator2D(
        LINEAR, LINEAR));
    //    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Rho data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final double shift = 0.0001;
    return createSABR1RhoBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(final double shift) {
    return createSABR1RhoBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Alpha data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped() {
    final double shift = 0.0001;
    return createSABR1RhoBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, new GridInterpolator2D(
        LINEAR, LINEAR));
    //    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift,
        0.50 + shift, 0.50 + shift, 0.50 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift}, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final double shift = 0.0001;
    return createSABR1NuBumped(sabrFunction, shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Nu data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped(final double shift) {
    return createSABR1NuBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Nu data is bumped by 0.0001 with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1NuBumped() {
    final double shift = 0.0001;
    return createSABR1NuBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a SABR surface with extrapolation parameters based on createSABR1.
   * @param sabrFunction The SABR function.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   * @return The SABR surface with extrapolation parameters.
   */
  public static SABRInterestRateExtrapolationParameters createSABRExtrapolation1(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double cutOffStrike, final double mu) {
    final SABRInterestRateParameters sabr = createSABR1();
    return SABRInterestRateExtrapolationParameters.from(sabr, cutOffStrike, mu);
  }

  /**
   * Create a SABR surface with extrapolation and with Hagan volatility function parameters based on createSABR1.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   * @return The SABR surface with extrapolation parameters.
   */
  public static SABRInterestRateExtrapolationParameters createSABRExtrapolation1(final double cutOffStrike, final double mu) {
    return createSABRExtrapolation1(new SABRHaganVolatilityFunction(), cutOffStrike, mu);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated and flat extrapolated) with a given SABR function. 
   * The expirations and tenors are not on a full grid (short expiries with shorter tenors).
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR2() {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.05,
        0.06, 0.07, 0.04, 0.05, 0.06, 0.07, 0.03, 0.04, 0.05, 0.06, 0.07, 0.03, 0.04, 0.05, 0.06, 0.03, 0.04, 0.05, 0.06, 0.04, 0.05, 0.06}, new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT));
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.5, 0.5,
        0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT));
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {-0.25,
        -0.25, -0.25, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.10, 0.10, 0.10},
        new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT));
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0,
        10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.50, 0.50,
        0.50, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35}, new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT));
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, new SABRHaganVolatilityFunction());
  }

  /**
   * Create a yield curve bundle with two curves. One called "Funding" with a constant rate of 5% and one called "Forward" with a constant rate of 4%;
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurves1() {
    final String FUNDING_CURVE_NAME = "Funding";
    final String FORWARD_CURVE_NAME = "Forward";
    final YieldAndDiscountCurve CURVE_5 = new YieldCurve(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_4 = new YieldCurve(ConstantDoublesCurve.from(0.04));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    return curves;
  }

  /**
   * Create a yield curve bundle with three curves. One called "Credit" with a constant rate of 5%, one called "Discounting" with a constant rate of 4%, 
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
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(CREDIT_CURVE_NAME, CURVE_5);
    curves.setCurve(DISCOUNTING_CURVE_NAME, CURVE_4);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_45);
    return curves;
  }

  /**
   * Create a yield curve bundle with three curves. One called "Credit" with a constant rate of 6%, one called "Discounting" with a constant rate of 5%, 
   * and one called "Forward" with a constant rate of 5.5%.
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesBond2() {
    final String CREDIT_CURVE_NAME = "Credit";
    final String DISCOUNTING_CURVE_NAME = "Repo";
    final String FORWARD_CURVE_NAME = "Forward";
    final YieldAndDiscountCurve CURVE_6 = new YieldCurve(ConstantDoublesCurve.from(0.06));
    final YieldAndDiscountCurve CURVE_5 = new YieldCurve(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_55 = new YieldCurve(ConstantDoublesCurve.from(0.0550));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(CREDIT_CURVE_NAME, CURVE_6);
    curves.setCurve(DISCOUNTING_CURVE_NAME, CURVE_5);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_55);
    return curves;
  }

}
