/**
 * Copyright (C) 2009 - 2012 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * Data sets used in some tests related to G2++ model.
 * @deprecated {@link YieldCurveBundle} is deprecated, as are the classes that use it.
 */
@Deprecated
public class TestsDataSetsG2pp {

  private static final String DISCOUNTING = "Discounting";
  private static final String FORWARD3M = "Forward 3M";

  public static YieldCurveBundle createCurves1() {
    final InterpolatedDoublesCurve dscC = new InterpolatedDoublesCurve(new double[] {0.0, 50.0}, new double[] {0.0500, 0.0500}, CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, "Curve dsc");
    final InterpolatedDoublesCurve fwd3C = new InterpolatedDoublesCurve(new double[] {0.0, 50.0}, new double[] {0.0500, 0.0500}, CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, "Curve fwd3");
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DISCOUNTING, YieldCurve.from(dscC));
    curves.setCurve(FORWARD3M, YieldCurve.from(fwd3C));
    return curves;
  }

  public static YieldCurveBundle createCurves2() {
    final InterpolatedDoublesCurve dscC = new InterpolatedDoublesCurve(new double[] {0.0, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0}, new double[] {0.0100, 0.0150, 0.0200, 0.0250, 0.0300, 0.0350, 0.0400},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, "Curve dsc");
    final InterpolatedDoublesCurve fwd3C = new InterpolatedDoublesCurve(new double[] {0.0, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0}, new double[] {0.0120, 0.0170, 0.0220, 0.0270, 0.0320, 0.0370, 0.0420},
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR), true, "Curve fwd3");
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DISCOUNTING, YieldCurve.from(dscC));
    curves.setCurve(FORWARD3M, YieldCurve.from(fwd3C));
    return curves;
  }

  public static String[] curvesNames() {
    return new String[] {DISCOUNTING, FORWARD3M};
  }

  private static final double[] MEAN_REVERSION = new double[] {0.01, 0.10};
  private static final double[][] VOLATILITY = new double[][] { {0.01, 0.011, 0.012, 0.013, 0.014}, {0.01, 0.009, 0.008, 0.007, 0.006}};
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0};
  private static final double CORRELATION = -0.30;
  private static final G2ppPiecewiseConstantParameters G2PP_PARAMETERS = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME, CORRELATION);
  private static final double[][] VOLATILITY_CST_1 = new double[][] { {0.0120}, {0.0040}}; // 20%
  //  private static final double[][] VOLATILITY_CST_1 = new double[][] { {0.0108}, {0.0036}}; // 20%
  private static final G2ppPiecewiseConstantParameters G2PP_PARAMETERS_CST_1 = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY_CST_1, new double[0], CORRELATION);
  private static final double[][] VOLATILITY_CST_2 = new double[][] { {0.0180}, {0.0060}}; // 30%
  private static final G2ppPiecewiseConstantParameters G2PP_PARAMETERS_CST_2 = new G2ppPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY_CST_2, new double[0], CORRELATION);
  private static final HullWhiteOneFactorPiecewiseConstantParameters HULLWHITE_PARAMETERS_CST = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION[0], VOLATILITY_CST_1[0], new double[0]);

  /**
   * Create a set of G2++ parameters for testing.
   * @return The parameters.
   */
  public static G2ppPiecewiseConstantParameters createG2ppParameters() {
    return G2PP_PARAMETERS;
  }

  /**
   * Create a set of time-constant G2++ parameters for testing.
   * @return The parameters.
   */
  public static G2ppPiecewiseConstantParameters createG2ppCstParameters1() {
    return G2PP_PARAMETERS_CST_1;
  }

  /**
   * Create a set of time-constant G2++ parameters for testing.
   * @return The parameters.
   */
  public static G2ppPiecewiseConstantParameters createG2ppCstParameters2() {
    return G2PP_PARAMETERS_CST_2;
  }

  public static HullWhiteOneFactorPiecewiseConstantParameters createHullWhiteCstParameters() {
    return HULLWHITE_PARAMETERS_CST;
  }

}
