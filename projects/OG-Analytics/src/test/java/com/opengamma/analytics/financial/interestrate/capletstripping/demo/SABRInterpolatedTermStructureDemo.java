/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.demo;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStripper;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStripperSABRModel;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingResult;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingSetup;
import com.opengamma.analytics.financial.interestrate.capletstripping.MarketDataType;
import com.opengamma.analytics.financial.interestrate.capletstripping.MultiCapFloorPricer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;

/**
 * Here the volatility of a particular caplet is given by the SABR model; the parameters of the model
 * (alpha, beta, rho & nu) are given by interpolated parameter term structures (beta is forced to be flat).
 * Since SABR can at most fit 4 volatilities (for a common expiry at different strikes), it is not possible to recover
 * all the cap values. The result is a very smooth caplet volatility surface (apart from the low expiry-strike corner)
 * that only recovers cap volatilities to around 100bps.
 */
public class SABRInterpolatedTermStructureDemo extends CapletStrippingSetup {

  private static int NUM_SABR_PARMS = 4;
  private static String[] PARAMETER_NAMES = new String[] {"ALPHA", "BETA", "RHO", "NU" };
  private static Map<String, double[]> KNOTS;
  private static Map<String, ParameterLimitsTransform> TRANSFORMS;
  private static Map<String, Interpolator1D> INTERPOLATORS;
  private static DoublesVectorFunctionProvider[] VF_PROV;
  private static Interpolator1D DQ_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static Interpolator1D FLAT_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final DoubleMatrix1D START;

  static {

    KNOTS = new LinkedHashMap<>(NUM_SABR_PARMS);
    KNOTS.put(PARAMETER_NAMES[0], new double[] {1, 2, 3, 5, 7, 10 });
    KNOTS.put(PARAMETER_NAMES[1], new double[] {1 });
    KNOTS.put(PARAMETER_NAMES[2], new double[] {1, 3, 7, });
    KNOTS.put(PARAMETER_NAMES[3], new double[] {1, 2, 3, 5, 7, 10 });

    TRANSFORMS = new LinkedHashMap<>(NUM_SABR_PARMS);
    TRANSFORMS.put(PARAMETER_NAMES[0], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN));
    TRANSFORMS.put(PARAMETER_NAMES[1], new DoubleRangeLimitTransform(0.1, 1));
    TRANSFORMS.put(PARAMETER_NAMES[2], new DoubleRangeLimitTransform(-1, 1));
    TRANSFORMS.put(PARAMETER_NAMES[3], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN));

    final double[] startVal = new double[] {0.2, 0.7, -0.2, 0.5 };
    int nKnots = 0;
    for (int i = 0; i < NUM_SABR_PARMS; i++) {
      nKnots += KNOTS.get(PARAMETER_NAMES[i]).length;
    }
    START = new DoubleMatrix1D(nKnots);
    int pos = 0;
    for (int i = 0; i < NUM_SABR_PARMS; i++) {
      final int length = KNOTS.get(PARAMETER_NAMES[i]).length;
      Arrays.fill(START.getData(), pos, pos + length, TRANSFORMS.get(PARAMETER_NAMES[i]).transform(startVal[i]));
      pos += length;
    }

    INTERPOLATORS = new LinkedHashMap<>(NUM_SABR_PARMS);
    VF_PROV = new DoublesVectorFunctionProvider[NUM_SABR_PARMS];
    for (int i = 0; i < NUM_SABR_PARMS; i++) {
      final String name = PARAMETER_NAMES[i];
      final ParameterLimitsTransform trans = TRANSFORMS.get(name);
      if (name.equals("BETA")) {
        INTERPOLATORS.put(name, new TransformedInterpolator1D(FLAT_INTERPOLATOR, trans));
      } else {
        INTERPOLATORS.put(name, new TransformedInterpolator1D(DQ_INTERPOLATOR, trans));
      }
      VF_PROV[i] = new InterpolatedVectorFunctionProvider(INTERPOLATORS.get(name), KNOTS.get(name));
    }

  }

  /**
   * This fits all caps (including ATM) by adjusting the knot values of the interpolation (SABR) parameter term structures
   * <p>
   * The output is this surface sampled on a grid (101 by 101), such that it can be plotted as an Excel surface plot (or imported into some other visualisation tool).
   */
  @Test(description = "Demo of infering a caplet volatility surface")
  public void test() {

    final MulticurveProviderDiscount yc = getYieldCurves();
    final List<CapFloor> allCaps = getAllCaps();
    final double[] vols = getAllCapVols();

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(allCaps, yc);

    final double[] t = pricer.getCapletExpiries();

    final double[] errors = new double[allCaps.size()];
    Arrays.fill(errors, 0.01); // 100bps

    final CapletStripper stripper = new CapletStripperSABRModel(pricer, VF_PROV);
    final CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, errors, START);
    System.out.println(res);

    // Print out the market and model cap volatilities
    final double[] modelVols = res.getModelCapVols();
    final int n = allCaps.size();
    System.out.println("Market and model cap volatilities");
    System.out.println("Strike\tExpiry\tMarket Vol\tModel Vol");
    for (int i = 0; i < n; i++) {
      final CapFloor cap = allCaps.get(i);
      System.out.println(cap.getStrike() + "\t" + cap.getEndTime() + "\t" + vols[i] + "\t" + modelVols[i]);
    }

    // print the caplet volatilities
    System.out.println("\n");
    res.printCapletVols(System.out);

    // print caplet volatility surface
    System.out.println("Caplet volatility Surface");
    res.printSurface(System.out, 101, 101);
    System.out.println();

    // make the (calibrated) SABR parameter term structures, in order to print smooth curves
    final DoubleMatrix1D fitParms = res.getFitParameters();
    final InterpolatedDoublesCurve[] curves = new InterpolatedDoublesCurve[NUM_SABR_PARMS];
    int pos = 0;
    for (int i = 0; i < NUM_SABR_PARMS; i++) {
      final String name = PARAMETER_NAMES[i];
      final double[] knots = KNOTS.get(name);
      final int length = knots.length;
      final double[] y = new double[length];
      System.arraycopy(fitParms.getData(), pos, y, 0, length);
      pos += length;
      curves[i] = new InterpolatedDoublesCurve(knots, y, INTERPOLATORS.get(name), true);
    }

    System.out.println("SABR parameter term structures");
    System.out.println("time\talpha\tbeta\trho\tnu");
    for (int i = 0; i < 101; i++) {
      final double time = t[t.length - 1] * i / (100.0);
      System.out.print(time);
      for (int j = 0; j < NUM_SABR_PARMS; j++) {
        System.out.print("\t" + curves[j].getYValue(time));
      }
      System.out.println();
    }

  }
}
