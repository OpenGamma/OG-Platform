/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * Sets of market data used in tests.
 * @deprecated {@link YieldCurveBundle} is deprecated, as are the classes that use it.
 */
@Deprecated
public class TestsDataSetsInflation {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_LINEAR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", CALENDAR);

  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_TEN = InterpolatedDoublesSurface.from(
      new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0 },
      new double[] {2, 2, 2, 10, 10, 10 },
      new double[] {0.35, 0.34, 0.25, 0.30, 0.25, 0.20 },
      INTERPOLATOR_LINEAR_2D);
  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_STR = InterpolatedDoublesSurface.from(
      new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0, 0.5, 1.0, 5.0 },
      new double[] {0.01, 0.01, 0.01, 0.02, 0.02, 0.02, 0.03, 0.03, 0.03 },
      new double[] {0.35, 0.34, 0.25, 0.30, 0.25, 0.20, 0.28, 0.23, 0.18 },
      INTERPOLATOR_LINEAR_2D);
  private static final BlackFlatSwaptionParameters BLACK_SWAPTION_EUR6 = new BlackFlatSwaptionParameters(BLACK_SURFACE_EXP_TEN, EUR1YEURIBOR6M);
  private static final BlackFlatSwaptionParameters BLACK_SWAPTION_EUR3 = new BlackFlatSwaptionParameters(BLACK_SURFACE_EXP_TEN, EUR1YEURIBOR3M);

  public static InterpolatedDoublesSurface createBlackSurfaceExpiryTenor() {
    return BLACK_SURFACE_EXP_TEN;
  }

  public static InterpolatedDoublesSurface createBlackSurfaceExpiryStrike() {
    return BLACK_SURFACE_EXP_STR;
  }

  public static InterpolatedDoublesSurface createBlackSurfaceExpiryTenorShift(final double shift) {
    return InterpolatedDoublesSurface.from(new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0 }, new double[] {2, 2, 2, 10, 10, 10 }, new double[] {0.35 + shift, 0.34 + shift, 0.25 + shift, 0.30 + shift,
        0.25 + shift, 0.20 + shift }, INTERPOLATOR_LINEAR_2D);
  }

  public static BlackFlatSwaptionParameters createBlackSwaptionEUR6() {
    return BLACK_SWAPTION_EUR6;
  }

  public static BlackFlatSwaptionParameters createBlackSwaptionEUR3() {
    return BLACK_SWAPTION_EUR3;
  }

  /**
   * Create the same surface as createBlackSwaptionEUR6() but with a given parallel shift.
   * @param shift The shift.
   * @return The surface.
   */
  public static BlackFlatSwaptionParameters createBlackSwaptionEUR6Shift(final double shift) {
    final InterpolatedDoublesSurface surfaceShift = createBlackSurfaceExpiryTenorShift(shift);
    return new BlackFlatSwaptionParameters(surfaceShift, EUR1YEURIBOR6M);
  }

  /**
   * Create the same surface as createBlackSwaptionEUR6() but with one volatility shifted.
   * @param index The index of the shifted volatility.
   * @param shift The shift.
   * @return The surface.
   */
  public static BlackFlatSwaptionParameters createBlackSwaptionEUR6Shift(final int index, final double shift) {
    final double[] vol = new double[] {0.35, 0.34, 0.25, 0.30, 0.25, 0.20 };
    vol[index] += shift;
    final InterpolatedDoublesSurface surfaceShift = InterpolatedDoublesSurface.from(new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0 }, new double[] {2, 2, 2, 10, 10, 10 }, vol, INTERPOLATOR_LINEAR_2D);
    return new BlackFlatSwaptionParameters(surfaceShift, EUR1YEURIBOR6M);
  }

  public static YieldCurveBundle createCurvesEUR() {
    final String discountingCurvename = "EUR Discounting";
    final String forward3MCurveName = "Forward EURIBOR3M";
    final String forward6MCurveName = "Forward EURIBOR6M";
    final InterpolatedDoublesCurve dscC = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 20.0 }, new double[] {0.0050, 0.0100, 0.0150, 0.0200, 0.0200, 0.0300 },
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, discountingCurvename);
    final InterpolatedDoublesCurve fwd3C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 25.0 }, new double[] {0.0070, 0.0120, 0.0165, 0.0215, 0.0210, 0.0310 },
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward3MCurveName);
    final InterpolatedDoublesCurve fwd6C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 30.0 }, new double[] {0.0075, 0.0125, 0.0170, 0.0220, 0.0212, 0.0312 },
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward6MCurveName);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(discountingCurvename, YieldCurve.from(dscC));
    curves.setCurve(forward3MCurveName, YieldCurve.from(fwd3C));
    curves.setCurve(forward6MCurveName, YieldCurve.from(fwd6C));
    return curves;
  }

  public static String[] curvesEURNames() {
    final String discountingCurvename = "EUR Discounting";
    final String forward3MCurveName = "Forward EURIBOR3M";
    final String forward6MCurveName = "Forward EURIBOR6M";
    return new String[] {discountingCurvename, forward3MCurveName, forward6MCurveName };
  }

  public static YieldCurveBundle createCurvesUSD() {
    final String discountingCurvename = "USD Discounting";
    final String forward3MCurveName = "Forward USDLIBOR3M";
    final String forward6MCurveName = "Forward USDLIBOR6M";
    final InterpolatedDoublesCurve dscC = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 20.0 }, new double[] {0.0050, 0.0100, 0.0150, 0.0200, 0.0200, 0.0300 },
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, discountingCurvename);
    final InterpolatedDoublesCurve fwd3C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 25.0 }, new double[] {0.0070, 0.0120, 0.0165, 0.0215, 0.0210, 0.0310 },
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward3MCurveName);
    final InterpolatedDoublesCurve fwd6C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 30.0 }, new double[] {0.0075, 0.0125, 0.0170, 0.0220, 0.0212, 0.0312 },
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward6MCurveName);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(discountingCurvename, YieldCurve.from(dscC));
    curves.setCurve(forward3MCurveName, YieldCurve.from(fwd3C));
    curves.setCurve(forward6MCurveName, YieldCurve.from(fwd6C));
    return curves;
  }

  /**
   * Create a yield curve bundle with three curves. One called "Credit" with a constant rate of 5%, one called "Discounting" with a constant rate of 4%,
   * and one called "Forward" with a constant rate of 4.5%.
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesBond() {
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

  public static YieldCurveWithBlackCubeBundle createCubesBondFutureOption() {
    return new YieldCurveWithBlackCubeBundle(BLACK_SURFACE_EXP_TEN, createCurvesBond());
  }

}
