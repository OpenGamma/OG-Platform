/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.HashMap;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

/**
 * Sets of market data used in tests.
 * @deprecated {@link YieldCurveBundle} is deprecated, as are the classes that use it.
 */
@Deprecated
public class TestsDataSetsSABR {

  /**
   * The linear interpolator/ flat extrapolator.  Used for SABR parameters interpolation.
   */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  /**
   * The standard day count 30/360 used in the data set.
   */
  private static final DayCount DAY_COUNT = DayCounts.THIRTY_U_360;

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Expiry is between 0 and 10 years, maturity between 0 and 10 years.
   * Beta is 0.5.  Alpha 0.05 at 1Y and 0.06 at 10Y. Rho 0.50 at 1Y and 0.30 at 10Y. Nu -0.25 at 1Y and 0.00 at 10Y.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10},
        new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
            0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
        0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25,
        -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50,
        0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, INTERPOLATOR_2D);
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
        0.05 + shift, 0.05 + shift, 0.05 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift, 0.06 + shift}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, INTERPOLATOR_2D);
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
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Beta data is bumped by a given shift with respect to SABR1.
   * @param sabrFunction The SABR function.
   * @param shift The shift.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1BetaBumped(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction, final double shift) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10},
        new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
            0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.5 + shift, 0.5 + shift, 0.5 + shift,
        0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift,
        0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift, 0.5 + shift}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25,
        -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5,
        10, 100}, new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50,
        0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, sabrFunction);
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
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift, -0.25 + shift,
        -0.25 + shift, -0.25 + shift, -0.25 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift, 0.00 + shift}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30}, INTERPOLATOR_2D);
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
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
        INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10}, new double[] {0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10}, new double[] {0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift, 0.50 + shift,
        0.50 + shift, 0.50 + shift, 0.50 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift, 0.30 + shift}, INTERPOLATOR_2D);
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
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.05,
        0.06, 0.07, 0.04, 0.05, 0.06, 0.07, 0.03, 0.04, 0.05, 0.06, 0.07, 0.03, 0.04, 0.05, 0.06, 0.03, 0.04, 0.05, 0.06, 0.04, 0.05, 0.06}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.5, 0.5,
        0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {-0.25,
        -0.25, -0.25, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.10, 0.10, 0.10}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0,
        10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.50, 0.50,
        0.50, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35}, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, new SABRHaganVolatilityFunction());
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated and flat extrapolated) with a given SABR function.
   * The expirations and tenors are not on a full grid (short expiries with shorter tenors).
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR3() {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.03,
        0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.026, 0.026, 0.022, 0.020, 0.029, 0.028, 0.027, 0.026, 0.03, 0.031, 0.032}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.25,
        0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0,
        10.0, 10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {-0.15,
        -0.15, -0.15, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.10, -0.00, 0.25, 0.10, 0.40, 0.00, 0.00, 0.00, 0.00, 0.10, 0.10, 0.10}, INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.25, 0.25, 0.25, 0.50, 0.50, 0.50, 0.50, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0,
        10.0, 20.0, 20.0, 20.0}, new double[] {1.0, 2.0, 5.0, 1.0, 2.0, 5.0, 10.0, 1.0, 2.0, 5.0, 10.0, 20, 2.0, 5.0, 10.0, 20.0, 2.0, 5.0, 10.0, 20.0, 5.0, 10.0, 20.0}, new double[] {0.50, 0.50,
        0.50, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.40, 0.45, 0.25, 0.25, 0.40, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35}, INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT, new SABRHaganVolatilityFunction());
  }

  /**
   * Create a yield curve bundle with two curves. One called "Funding" with a constant rate of 5% and one called "Forward" with a constant rate of 4%;
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurves1() {
    final String FUNDING_CURVE_NAME = "Funding";
    final String FORWARD_CURVE_NAME = "Forward";
    final YieldAndDiscountCurve CURVE_5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    return curves;
  }

  public static String[] curves1Names() {
    final String FUNDING_CURVE_NAME = "Funding";
    final String FORWARD_CURVE_NAME = "Forward";
    return new String[] {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  }

  /**
   * Creates a yield curve bundle with 3 interpolated curves: Discounting, Forward 3M, and Forward 6M.
   * @return The bundle.
   */
  public static YieldCurveBundle createCurves2() {
    return createCurves2(Currency.EUR);
  }

  /**
   * Creates a yield curve bundle with 3 interpolated curves: Discounting, Forward 3M, and Forward 6M.
   * @return The bundle.
   */
  public static YieldCurveBundle createCurves2(final Currency ccy) {
    final String discountingCurvename = "Discounting";
    final String forward3MCurveName = "Forward 3M";
    final String forward6MCurveName = "Forward 6M";
    final HashMap<String, Currency> ccyMap = new HashMap<>();
    ccyMap.put(discountingCurvename, ccy);
    ccyMap.put(forward3MCurveName, ccy);
    ccyMap.put(forward6MCurveName, ccy);
    final FXMatrix fx = new FXMatrix(ccy);
    final InterpolatedDoublesCurve dscC = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 20.0}, new double[] {0.0050, 0.0100, 0.0150, 0.0200, 0.0200, 0.0300},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, "Curve dsc");
    final InterpolatedDoublesCurve fwd3C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 25.0}, new double[] {0.0070, 0.0120, 0.0165, 0.0215, 0.0210, 0.0310},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, "Curve fwd3");
    final InterpolatedDoublesCurve fwd6C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 30.0}, new double[] {0.0075, 0.0125, 0.0170, 0.0220, 0.0212, 0.0312},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, "Curve fwd6");
    final YieldCurveBundle curves = new YieldCurveBundle(fx, ccyMap);
    curves.setCurve(discountingCurvename, YieldCurve.from(dscC));
    curves.setCurve(forward3MCurveName, YieldCurve.from(fwd3C));
    curves.setCurve(forward6MCurveName, YieldCurve.from(fwd6C));
    return curves;
  }

  public static String[] curves2Names() {
    final String discountingCurvename = "Discounting";
    final String forward3MCurveName = "Forward 3M";
    final String forward6MCurveName = "Forward 6M";
    return new String[] {discountingCurvename, forward3MCurveName, forward6MCurveName};
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
    final YieldAndDiscountCurve CURVE_5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    final YieldAndDiscountCurve CURVE_45 = YieldCurve.from(ConstantDoublesCurve.from(0.045));
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
    final YieldAndDiscountCurve CURVE_6 = YieldCurve.from(ConstantDoublesCurve.from(0.06));
    final YieldAndDiscountCurve CURVE_5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_55 = YieldCurve.from(ConstantDoublesCurve.from(0.0550));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(CREDIT_CURVE_NAME, CURVE_6);
    curves.setCurve(DISCOUNTING_CURVE_NAME, CURVE_5);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_55);
    return curves;
  }

  /**
   * Create a yield curve bundle with three curves. One called "EUR Credit" with a constant rate of 5%, one called "EUR Discounting" with a constant rate of 4%.
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesBond3() {
    final String DISCOUNTING_CURVE_NAME = "EUR Discounting";
    final String CREDIT_CURVE_NAME = "EUR Credit";
    final YieldAndDiscountCurve CURVE_5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(CREDIT_CURVE_NAME, CURVE_5);
    curves.setCurve(DISCOUNTING_CURVE_NAME, CURVE_4);
    return curves;
  }

  public static String[] nameCurvesBond3() {
    final String DISCOUNTING_CURVE_NAME = "EUR Discounting";
    final String CREDIT_CURVE_NAME = "EUR Credit";
    return new String[] {DISCOUNTING_CURVE_NAME, CREDIT_CURVE_NAME};
  }

}
