/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloorPricer;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStrippingSetup;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class SABRFittingTest extends CapletStrippingSetup {

  private static double[][] NODES;
  private static LinkedHashMap<String, double[]> CURVE_NODES;
  private static LinkedHashMap<String, Interpolator1D> INTERPOLATORS;
  private static LinkedHashMap<String, ParameterLimitsTransform> TRANSFORMS;
  private static String[] NAMES = new String[] {"alpha", "beta", "rho", "nu" };
  private static final DoubleMatrix1D START;

  private static final VolatilityModelProvider VOL_MODEL_PROVIDER;
  private static final LinkedHashMap<String, DoublesCurve> KNOWN_CURVES;

  static {

    final double[] temp = getCapEndTimes();
    final int n = temp.length;
    NODES = new double[][] { {1, 2, 3, 5, 7, 10 }, {10 }, {1, 3, 7 }, {1, 2, 3, 5, 7, 10 } }; //new double[n + 1];
    // System.arraycopy(temp, 0, NODES, 1, n);

    TRANSFORMS = new LinkedHashMap<>();
    TRANSFORMS.put(NAMES[0], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN)); // alpha > 0
    TRANSFORMS.put(NAMES[1], new DoubleRangeLimitTransform(0.1, 1)); // 0<beta<1
    TRANSFORMS.put(NAMES[2], new DoubleRangeLimitTransform(-1, 1)); // -0.95<rho<0.95
    TRANSFORMS.put(NAMES[3], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN));
    final ConstantDoublesCurve beta = ConstantDoublesCurve.from(0.6);
    KNOWN_CURVES = new LinkedHashMap<>(1);
    KNOWN_CURVES.put("beta", beta);

    CURVE_NODES = new LinkedHashMap<>();
    INTERPOLATORS = new LinkedHashMap<>();
    final CombinedInterpolatorExtrapolator baseInterpolator = CombinedInterpolatorExtrapolatorFactory
        .getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    //final DoubleQuadraticInterpolator1D baseInterpolator = new DoubleQuadraticInterpolator1D();
    final CombinedInterpolatorExtrapolator betaBaseInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

    int index = 0;
    int totalNodes = 0;
    for (final String name : NAMES) {
      totalNodes += NODES[index].length;
      CURVE_NODES.put(name, NODES[index++]);
      if (name.compareTo("beta") == 0) {
        INTERPOLATORS.put(name, betaBaseInterpolator);
      } else {
        INTERPOLATORS.put(name, baseInterpolator);
      }
    }

    // start from some realistic values, and transform these into the fitting parameters
    final double[] start = new double[totalNodes];
    final double[] modelStartValues = new double[] {0.1, 0.7, -0.2, 0.5 };
    int a = 0;
    int b = 0;
    for (int i = 0; i < TRANSFORMS.size(); i++) {
      b += NODES[i].length;
      Arrays.fill(start, a, b, TRANSFORMS.get(NAMES[i]).transform(modelStartValues[i]));
      a = b;
    }
    START = new DoubleMatrix1D(start);

    VOL_MODEL_PROVIDER = new SABRTermStructureModelProvider(CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);

  }

  @Test
  //(enabled = false)
  public void test() {
    final MulticurveProviderDiscount yc = getYieldCurves();
    final List<CapFloor> caps = getAllCaps();
    final double[] vols = getAllCapVols();

    final CapletStrippingFunction func = new CapletStrippingFunction(caps, yc, VOL_MODEL_PROVIDER);
    final CapletStrippingJacobian jac = new CapletStrippingJacobian(caps, yc, CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);

    final DoubleMatrix1D startVols = func.evaluate(START);

    final double[] sigma = new double[caps.size()];
    Arrays.fill(sigma, 0.0001); // 1bps

    final NonLinearLeastSquare ls = new NonLinearLeastSquare();
    final LeastSquareResults lsRes = ls.solve(new DoubleMatrix1D(vols), new DoubleMatrix1D(sigma), func, START);

    System.out.println("chi2: " + lsRes.getChiSq());

    final DoubleMatrix1D fitParms = lsRes.getFitParameters();
    final DoubleMatrix1D modelVols = func.evaluate(fitParms);

    System.out.println();
    final int n = caps.size();
    for (int i = 0; i < n; i++) {
      final CapFloor cap = caps.get(i);
      System.out.println(cap.getStrike() + "\t" + cap.getEndTime() + "\t" + vols[i] + "\t" + modelVols.getEntry(i));
    }

    System.out.println();
    final VolatilityModel1D volSurface = VOL_MODEL_PROVIDER.evaluate(fitParms);

    System.out.println("t\talpha\tbeta\tnu\trho");
    final LinkedHashMap<String, ? extends DoublesCurve> curves = ((SABRTermStructureModelProvider) VOL_MODEL_PROVIDER).getCurves(fitParms);
    final DoublesCurve alphaCurve = curves.get("alpha");
    final DoublesCurve betaCurve = curves.get("beta");
    final DoublesCurve nuCurve = curves.get("nu");
    final DoublesCurve rhoCurve = curves.get("rho");
    for (int i = 0; i < 100; i++) {
      final double t = 10. * i / 99.;
      final double alpha = alphaCurve.getYValue(t);
      final double beta = betaCurve.getYValue(t);
      final double nu = nuCurve.getYValue(t);
      final double rho = rhoCurve.getYValue(t);
      System.out.println(t + "\t" + alpha + "\t" + beta + "\t" + nu + "\t" + rho);
    }
    System.out.println();

    //Awkward way of getting the caplet expiries and (Libor) forward rates 
    final List<CapFloor> atmCaps = getATMCaps();
    final CapFloorPricer pricer = new CapFloorPricer(atmCaps.get(atmCaps.size() - 1), yc);
    final double[] fwds = pricer.getForwards();
    final double[] exp = pricer.getExpiries();

    final int nStrikeSamples = 100;
    final int nTimeSamples = exp.length;
    for (int i = 0; i < nTimeSamples; i++) {
      System.out.print("\t" + exp[i]);
    }
    System.out.print("\n");
    for (int j = 0; j < nStrikeSamples; j++) {
      final double k = 0.001 + 0.12 * j / (nStrikeSamples - 1.0);
      System.out.print(k);
      for (int i = 0; i < nTimeSamples; i++) {
        final double vol = volSurface.getVolatility(fwds[i], k, exp[i]);
        System.out.print("\t" + vol);
      }
      System.out.print("\n");
    }

  }

}
