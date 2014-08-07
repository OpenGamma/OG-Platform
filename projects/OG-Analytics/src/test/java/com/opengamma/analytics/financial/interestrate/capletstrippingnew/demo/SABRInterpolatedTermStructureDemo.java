/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew.demo;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStripper;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStripperSmileModel;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStrippingResult;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStrippingSetup;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.ConcatenatedVectorFunction;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MarketDataType;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MultiCapFloorPricer;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.VectorFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
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
 * that only recovers cap volatilities  to around 100bps.  
 */
public class SABRInterpolatedTermStructureDemo extends CapletStrippingSetup {

  private static int NUM_SABR_PARMS = 4;
  private static String[] PARAMETER_NAMES = new String[] {"ALPHA", "BETA", "RHO", "NU" };
  private static Map<String, double[]> KNOTS;
  private static Map<String, ParameterLimitsTransform> TRANSFORMS;
  private static Map<String, Interpolator1D> INTERPOLATORS;
  private static Interpolator1D BASE_INTERPOLATOR;

  private static final DoubleMatrix1D START;

  static {
    BASE_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
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
      Arrays.fill(START.getData(), pos, length, startVal[i]);
      pos += length;
    }

    INTERPOLATORS = new LinkedHashMap<>(NUM_SABR_PARMS);
    for (int i = 0; i < NUM_SABR_PARMS; i++) {
      final String name = PARAMETER_NAMES[i];
      final ParameterLimitsTransform trans = TRANSFORMS.get(name);
      INTERPOLATORS.put(name, new TransformedInterpolator1D(BASE_INTERPOLATOR, trans));
    }
  }

  /**
   * This fits all caps (including ATM) by adjusting the knot values of the interpolation (SABR) parameter term structures 
   */
  @Test
  public void test() {

    final MulticurveProviderDiscount yc = getYieldCurves();
    final List<CapFloor> allCaps = getAllCaps();
    final double[] vols = getAllCapVols();

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(allCaps, yc);

    final InterpolatedVectorFunctionProvider alphaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, ALPHA_TRANSFORM), ALPHA_KNOTS);
    final InterpolatedVectorFunctionProvider betaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR), BETA_TRANSFORM), BETA_KNOTS);
    final InterpolatedVectorFunctionProvider rhoPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, RHO_TRANSFORM), RHO_KNOTS);
    final InterpolatedVectorFunctionProvider nuPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, NU_TRANSFORM), NU_KNOTS);

    final double[] t = pricer.getCapletExpiries();
    final VectorFunction alpha = alphaPro.from(t);
    final VectorFunction beta = betaPro.from(t);
    final VectorFunction rho = rhoPro.from(t);
    final VectorFunction nu = nuPro.from(t);

    final VectorFunction modelToSmileParms = new ConcatenatedVectorFunction(new VectorFunction[] {alpha, beta, rho, nu });

    final double[] errors = new double[allCaps.size()];
    Arrays.fill(errors, 0.01); // 100bps

    final CapletStripper stripper = new CapletStripperSmileModel<SABRFormulaData>(pricer, new SABRHaganVolatilityFunction(true), modelToSmileParms);
    final CapletStrippingResult res = stripper.solve(vols, MarketDataType.VOL, errors, START);
    System.out.println(res);

    final double[] modelVols = res.getModelCapVols();
    final int n = allCaps.size();
    for (int i = 0; i < n; i++) {
      final CapFloor cap = allCaps.get(i);
      System.out.println(cap.getStrike() + "\t" + cap.getEndTime() + "\t" + vols[i] + "\t" + modelVols[i]);
    }

    res.printSurface(System.out, 101, 101);

  }
}
