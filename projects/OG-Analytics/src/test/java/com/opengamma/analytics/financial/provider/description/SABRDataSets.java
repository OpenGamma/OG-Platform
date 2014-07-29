/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * Sets of market data used in tests.
 */
public class SABRDataSets {

  /**
   * The linear interpolator/ flat extrapolator.  Used for SABR parameters interpolation.
   */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Expiry is between 0 and 10 years, maturity between 0 and 10 years.
   * Beta is 0.5.  Alpha 0.05 at 1Y and 0.06 at 10Y. Rho 0.50 at 1Y and 0.30 at 10Y. Nu -0.25 at 1Y and 0.00 at 10Y.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 },
        new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100 }, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
          0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
      10, 100 }, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 }, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
      0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
      10, 100 }, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 }, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25,
      -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
      10, 100 }, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 }, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50,
      0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30 }, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
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
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0,
      0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift, 0.05 + shift,
      0.05 + shift, 0.05 + shift, 0.05 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 },
        INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1,
      1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30 }, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
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
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Beta data is bumped by a given shift with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1BetaBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 },
        new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100 }, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
          0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
      10, 100 }, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 }, new double[] {0.5 + shift, 0.5 + shift, 0.5 + shift,
      0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift,
      0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
      10, 100 }, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 }, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25,
      -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
      10, 100 }, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 }, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50,
      0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30 }, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function. Beta data is bumped by a given shift with respect to SABR1.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1BetaBumped(final double shift) {
    return createSABR1BetaBumped(new SABRHaganVolatilityFunction(), shift);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Rho data is bumped by the shift with respect to SABR1.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1RhoBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0,
      0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {-0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift,
      -0.25 + shift, -0.25 + shift, -0.25 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1,
      1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30 }, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
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
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0,
      0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 },
        INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 }, new double[] {0, 0, 0, 0, 0, 0,
      1,
      1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10 }, new double[] {0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift,
      0.50 + shift, 0.50 + shift, 0.50 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift }, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
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

  public static SABRInterestRateParameters createSABR1ParameterBumped(final double shift, final int parameterNumber) {
    switch (parameterNumber) {
      case 0:
        return createSABR1AlphaBumped(new SABRHaganVolatilityFunction(), shift);
      case 1:
        return createSABR1RhoBumped(new SABRHaganVolatilityFunction(), shift);
      case 2:
        return createSABR1NuBumped(new SABRHaganVolatilityFunction(), shift);
      default:
        return null;
    }
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated and flat extrapolated) with a given SABR function.
   * The expirations and tenors are not on a full grid (short expiries with shorter tenors).
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR2() {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
      10.0, 10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {0.05,
      0.06, 0.07, 0.04, 0.05, 0.06, 0.07, 0.03, 0.04, 0.05, 0.06, 0.07, 0.03, 0.04, 0.05, 0.06, 0.03, 0.04, 0.05, 0.06, 0.04, 0.05, 0.06 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
      10.0, 10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {0.5, 0.5,
      0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
      10.0, 10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {-0.25,
      -0.25, -0.25, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.10, 0.10, 0.10 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0,
      10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {0.50, 0.50,
      0.50, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35 }, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated and flat extrapolated) with a given SABR function.
   * The expirations and tenors are not on a full grid (short expiries with shorter tenors).
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR3() {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
      10.0, 10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {0.03,
      0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.026, 0.026, 0.022, 0.020, 0.029, 0.028, 0.027, 0.026, 0.03, 0.031, 0.032 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
      10.0, 10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {0.25,
      0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
      10.0, 10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {-0.15,
      -0.15, -0.15, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.00, 0.25, 0.10, 0.40, 0.00, 0.00, 0.00, 0.00, 0.10, 0.10, 0.10 }, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0,
      10.0, 20.0, 20.0, 20.0 }, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0 }, new double[] {0.50, 0.50,
      0.50, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.45, 0.25, 0.25, 0.40, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35 }, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, new SABRHaganVolatilityFunction());
  }

}
