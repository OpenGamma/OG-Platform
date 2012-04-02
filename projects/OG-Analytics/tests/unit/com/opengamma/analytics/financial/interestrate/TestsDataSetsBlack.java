/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwap;
import com.opengamma.analytics.financial.instrument.index.generator.EUR1YEURIBOR3M;
import com.opengamma.analytics.financial.instrument.index.generator.EUR1YEURIBOR6M;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.BlackSwaptionParameters;
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
 */
public class TestsDataSetsBlack {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwap EUR1YEURIBOR6M = new EUR1YEURIBOR6M(CALENDAR);
  private static final GeneratorSwap EUR1YEURIBOR3M = new EUR1YEURIBOR3M(CALENDAR);

  private static final InterpolatedDoublesSurface BLACK_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0}, new double[] {2, 2, 2, 10, 10, 10}, new double[] {0.35,
      0.34, 0.25, 0.30, 0.25, 0.20}, INTERPOLATOR_2D);
  private static final BlackSwaptionParameters BLACK_SWAPTION_EUR6 = new BlackSwaptionParameters(BLACK_SURFACE, EUR1YEURIBOR6M);
  private static final BlackSwaptionParameters BLACK_SWAPTION_EUR3 = new BlackSwaptionParameters(BLACK_SURFACE, EUR1YEURIBOR3M);

  //  private static final double[] CUBE_X = new double[]{};
  //  private static final double[] CUBE_Y = new double[]{};
  //  private static final double[] CUBE_Z = new double[]{};
  //  private static final double[] CUBE_VALUES = new double[]{};
  //  private static final InterpolatorND INTERPOLATOR_2D = 
  //  private static final InterpolatedDoublesCube BLACK_CUBE = InterpolatedDoublesCube.from(CUBE_X, CUBE_Y, CUBE_Z, CUBE_VALUES, interpolator);

  public static InterpolatedDoublesSurface createBlackSurface() {
    return BLACK_SURFACE;
  }

  public static InterpolatedDoublesSurface createBlackSurfaceShift(final double shift) {
    return InterpolatedDoublesSurface.from(new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0}, new double[] {2, 2, 2, 10, 10, 10}, new double[] {0.35 + shift, 0.34 + shift, 0.25 + shift, 0.30 + shift,
        0.25 + shift, 0.20 + shift}, INTERPOLATOR_2D);
  }

  public static BlackSwaptionParameters createBlackSwaptionEUR6() {
    return BLACK_SWAPTION_EUR6;
  }

  public static BlackSwaptionParameters createBlackSwaptionEUR3() {
    return BLACK_SWAPTION_EUR3;
  }

  /**
   * Create the same surface as createBlackSwaptionEUR6() but with a given parallel shift.
   * @param shift The shift.
   * @return The surface.
   */
  public static BlackSwaptionParameters createBlackSwaptionEUR6Shift(final double shift) {
    InterpolatedDoublesSurface surfaceShift = createBlackSurfaceShift(shift);
    return new BlackSwaptionParameters(surfaceShift, EUR1YEURIBOR6M);
  }

  /**
   * Create the same surface as createBlackSwaptionEUR6() but with one volatility shifted.
   * @param index The index of the shifted volatility.
   * @param shift The shift.
   * @return The surface.
   */
  public static BlackSwaptionParameters createBlackSwaptionEUR6Shift(final int index, final double shift) {
    double[] vol = new double[] {0.35, 0.34, 0.25, 0.30, 0.25, 0.20};
    vol[index] += shift;
    InterpolatedDoublesSurface surfaceShift = InterpolatedDoublesSurface.from(new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0}, new double[] {2, 2, 2, 10, 10, 10}, vol, INTERPOLATOR_2D);
    return new BlackSwaptionParameters(surfaceShift, EUR1YEURIBOR6M);
  }

  public static YieldCurveBundle createCurvesEUR() {
    final String discountingCurvename = "EUR Discounting";
    final String forward3MCurveName = "Forward EURIBOR3M";
    final String forward6MCurveName = "Forward EURIBOR6M";
    InterpolatedDoublesCurve dscC = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 20.0}, new double[] {0.0050, 0.0100, 0.0150, 0.0200, 0.0200, 0.0300},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, discountingCurvename);
    InterpolatedDoublesCurve fwd3C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 25.0}, new double[] {0.0070, 0.0120, 0.0165, 0.0215, 0.0210, 0.0310},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward3MCurveName);
    InterpolatedDoublesCurve fwd6C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 30.0}, new double[] {0.0075, 0.0125, 0.0170, 0.0220, 0.0212, 0.0312},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward6MCurveName);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(discountingCurvename, new YieldCurve(dscC));
    curves.setCurve(forward3MCurveName, new YieldCurve(fwd3C));
    curves.setCurve(forward6MCurveName, new YieldCurve(fwd6C));
    return curves;
  }

  public static String[] curvesEURNames() {
    final String discountingCurvename = "EUR Discounting";
    final String forward3MCurveName = "Forward EURIBOR3M";
    final String forward6MCurveName = "Forward EURIBOR6M";
    return new String[] {discountingCurvename, forward3MCurveName, forward6MCurveName};
  }

  public static YieldCurveBundle createCurvesUSD() {
    final String discountingCurvename = "USD Discounting";
    final String forward3MCurveName = "Forward USDLIBOR3M";
    final String forward6MCurveName = "Forward USDLIBOR6M";
    InterpolatedDoublesCurve dscC = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 20.0}, new double[] {0.0050, 0.0100, 0.0150, 0.0200, 0.0200, 0.0300},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, discountingCurvename);
    InterpolatedDoublesCurve fwd3C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 25.0}, new double[] {0.0070, 0.0120, 0.0165, 0.0215, 0.0210, 0.0310},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward3MCurveName);
    InterpolatedDoublesCurve fwd6C = new InterpolatedDoublesCurve(new double[] {0.05, 1.0, 2.0, 5.0, 10.0, 30.0}, new double[] {0.0075, 0.0125, 0.0170, 0.0220, 0.0212, 0.0312},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, forward6MCurveName);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(discountingCurvename, new YieldCurve(dscC));
    curves.setCurve(forward3MCurveName, new YieldCurve(fwd3C));
    curves.setCurve(forward6MCurveName, new YieldCurve(fwd6C));
    return curves;
  }

}
