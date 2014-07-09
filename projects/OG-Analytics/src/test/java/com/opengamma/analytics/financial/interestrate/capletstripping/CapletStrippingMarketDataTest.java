/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.FlatVolatility;
import com.opengamma.analytics.financial.model.volatility.SABRTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapletStrippingMarketDataTest {

  private static final double[] INDEX_CURVE_NODES = new double[] {0.0438356164383561, 0.0876712328767123, 0.172602739726027, 0.254794520547945, 0.506849315068493, 0.758904109589041, 1.00547945205479,
      2.01369863013698, 3.01020285949547, 4.00547945205479, 5.00547945205479, 6.00547945205479, 7.01839958080694, 8.01095890410959, 9.00821917808219, 10.0082191780821, 15.0074706190583,
      20.0082191780821, 25.0109589041095, 30.0136986301369 };
  private static final double[] INDEX_CURVE_VALUES = new double[] {0.00184088091044285, 0.00201024117395892, 0.00241264832694067, 0.00280755413825359, 0.0029541307818572, 0.00310125437814943,
      0.00320054435838637, 0.00377914611772073, 0.00483320020067661, 0.00654829256979543, 0.00877749583222556, 0.0112470678648412, 0.0136301644164456, 0.0157618031582798, 0.0176836551757772,
      0.0194174141169365, 0.0254011614777518, 0.0282527762712854, 0.0298620063409043, 0.031116719228976 };

  private static final double[] DIS_CURVE_NODES = new double[] {0.00273972602739726, 0.0876712328767123, 0.172602739726027, 0.254794520547945, 0.345205479452054, 0.424657534246575, 0.506849315068493,
      0.758904109589041, 1.00547945205479, 2.00547945205479, 3.01020285949547, 4.00547945205479, 5.00547945205479, 10.0054794520547 };

  private static final double[] DIS_CURVE_VALUES = new double[] {0.00212916045658802, 0.00144265912946933, 0.00144567477491987, 0.00135441424749791, 0.00134009103595346, 0.00132773752749976,
      0.00127592397233014, 0.00132302501180961, 0.00138688847322639, 0.00172748279241698, 0.00254381216780551, 0.00410024606039574, 0.00628782387356631, 0.0170033466745807 };

  // cap/floor
  private static final int[] CAP_EXPIRIES = new int[] {1, 2, 5, 10 };

  private static final double[][] STRIKES = new double[][] {
      {0.05 },// , 0.01, 0.015, 0.02, 0.025, 0.03, 0.035, 0.04, 0.045, 0.05},
      {0.05 },// , 0.01, 0.015, 0.02, 0.025, 0.03, 0.035, 0.04, 0.045, 0.05, 0.055, 0.06},
      {0.05, 0.01, 0.015, 0.02, 0.025, 0.03, 0.035, 0.04, 0.045, 0.05, 0.055, 0.06, 0.07, 0.08, 0.09, 0.1, 0.11, 0.12 },
      {0.05, 0.01, 0.015, 0.02, 0.025, 0.03, 0.035, 0.04, 0.045, 0.05, 0.055, 0.06, 0.07, 0.08, 0.09, 0.1, 0.11, 0.12 } };

  private static final double[][] CAP_IMPLIED_VOL = new double[][] {
      {0.7311 },// , 0.9965, 0.9769, 1.02785, 0.9133, 0.987, 1.10224, 1.02362, 1.04179, 1.05794},
      {0.7941 },// , 0.8154, 0.78869, 0.8298, 0.80356, 0.8469, 0.81564, 0.8545, 0.82533, 0.8414, 0.8571, 0.8575},
      {0.8528, 0.71025, 0.63236, 0.612, 0.58151, 0.5658, 0.56404, 0.5388, 0.55844, 0.528, 0.5259, 0.5091, 0.5159, 0.5021, 0.5091, 0.5113, 0.4939, 0.4918 },
      {0.7356, 0.554, 0.47497, 0.4489, 0.39571, 0.3883, 0.36043, 0.3589, 0.34799, 0.3403, 0.3317, 0.3212, 0.317, 0.3148, 0.3189, 0.3346, 0.3332, 0.3322 } };

  private static final double[] CAP_ATM_VOL = new double[] {0.69165, 0.7639, 0.7346, 0.43646 };

  protected static final Currency CUR = Currency.USD;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int FREQUENCY = 4;
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  private static MulticurveProviderDiscount YIELD_CURVES;

  private static int N_CAPS;
  private static List<CapFloor> CAPS;
  private static double[] MARKET_VOLS;
  private static double[] SIGMA;

  private static String[] NAMES = new String[] {"alpha", "beta", "rho", "nu" };
  private static final double[] NODES = new double[] {0.0, 1.0, 2.0, 5.0, 10.0 };
  private static final VolatilityModelProvider VOL_MODEL_PROVIDER;
  private static LinkedHashMap<String, double[]> CURVE_NODES;
  private static LinkedHashMap<String, Interpolator1D> INTERPOLATORS;
  private static LinkedHashMap<String, ParameterLimitsTransform> TRANSFORMS;

  static {

    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC);
    final InterpolatedDoublesCurve curve1 = new InterpolatedDoublesCurve(INDEX_CURVE_NODES, INDEX_CURVE_VALUES, interpolator, true);
    final InterpolatedDoublesCurve curve2 = new InterpolatedDoublesCurve(DIS_CURVE_NODES, DIS_CURVE_VALUES, interpolator, true);
    // single curve for discount and projection (this is consistent with Bloomberg's cap quotes)
    final YieldCurve indexCurve = YieldCurve.from(curve1);
    final YieldCurve disCurve = YieldCurve.from(curve2);
    YIELD_CURVES = new MulticurveProviderDiscount();
    YIELD_CURVES.setCurve(CUR, disCurve);
    YIELD_CURVES.setCurve(INDEX, indexCurve);

    int temp = CAP_ATM_VOL.length;
    for (int i = 0; i < STRIKES.length; i++) {
      ArgumentChecker.isTrue(STRIKES[i].length == CAP_IMPLIED_VOL[i].length, "bad data");
      temp += STRIKES[i].length;
    }
    N_CAPS = temp;
    CAPS = new ArrayList<>(N_CAPS);
    MARKET_VOLS = new double[N_CAPS];
    SIGMA = new double[N_CAPS];
    // the ATM caps
    for (int i = 0; i < CAP_ATM_VOL.length; i++) {
      CapFloor cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, 0, FREQUENCY * CAP_EXPIRIES[i], 0.0, true);
      final CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
      final double fwd = pricer.getCapForward();
      cap = cap.withStrike(fwd);
      CAPS.add(cap);
      MARKET_VOLS[i] = CAP_ATM_VOL[i];
      SIGMA[i] = 0.0001; // 1bps error for ATM vols
    }

    // the OTM caps
    int count = CAP_EXPIRIES.length;
    final int start = 1; // miss out first (known) libor rate
    for (int i = 0; i < CAP_EXPIRIES.length; i++) {
      final int end = FREQUENCY * CAP_EXPIRIES[i];
      for (int j = 0; j < STRIKES[i].length; j++) {
        final CapFloor cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, start, end, STRIKES[i][j], true);
        CAPS.add(cap);
        MARKET_VOLS[count] = CAP_IMPLIED_VOL[i][j];
        SIGMA[count] = 0.01; // 100bps error for OTM vols
        count++;
      }
    }

    TRANSFORMS = new LinkedHashMap<>();
    TRANSFORMS.put(NAMES[0], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN)); // alpha > 0
    TRANSFORMS.put(NAMES[1], new DoubleRangeLimitTransform(0, 1)); // 0<beta<1
    TRANSFORMS.put(NAMES[2], new DoubleRangeLimitTransform(-0.8, 0.8)); // -0.95<rho<0.95
    TRANSFORMS.put(NAMES[3], new DoubleRangeLimitTransform(0.0, 5.0));
    // TRANSFORMS.put(NAMES[3], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN));

    CURVE_NODES = new LinkedHashMap<>();
    INTERPOLATORS = new LinkedHashMap<>();
    final DoubleQuadraticInterpolator1D baseInterpolator = new DoubleQuadraticInterpolator1D();
    for (final String name : NAMES) {
      if (name == "beta") {
        CURVE_NODES.put(name, new double[] {0.0 });
        final Interpolator1D flat = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
        INTERPOLATORS.put(name, flat);
      } else {
        CURVE_NODES.put(name, NODES);
        INTERPOLATORS.put(name, baseInterpolator);
      }
    }

    VOL_MODEL_PROVIDER = new SABRTermStructureModelProvider(CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);
  }

  @Test(enabled = false)
  public void testFwd() {

    final int start = 1; // miss out first (known) libor rate
    final int end = 5 * 4;

    // VolatilityModel1D volModel = new SABRTermStructureParameters(ConstantDoublesCurve.from(0.3), ConstantDoublesCurve.from(1.0), ConstantDoublesCurve.from(-0.5), ConstantDoublesCurve.from(0.5));
    final double vol = 0.688;
    final VolatilityModel1D volModel = new FlatVolatility(vol);

    CapFloor cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, start, end, 0.00928, true);
    CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
    double fwd = pricer.getCapForward();
    final double price = pricer.price(volModel);
    double impVol = pricer.impliedVol(volModel);
    System.out.println(fwd + "\t" + impVol + "\t" + price);

    cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, start, end, 0.03, true);
    pricer = new CapFloorPricer(cap, YIELD_CURVES);
    fwd = pricer.getCapForward();
    impVol = pricer.impliedVol(volModel);
    System.out.println(fwd + "\t" + impVol);
  }

  @Test
  //  (enabled = false)
  public void testStripping() {

    final CapletStrippingFunction func = new CapletStrippingFunction(CAPS, YIELD_CURVES, VOL_MODEL_PROVIDER);
    final CapletStrippingJacobian jac = new CapletStrippingJacobian(CAPS, YIELD_CURVES, CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);

    // start from some realistic values, and transform these into the fitting parameters
    final double[] start = new double[3 * NODES.length + 1];
    Arrays.fill(start, 0, NODES.length, TRANSFORMS.get(NAMES[0]).transform(0.1));
    Arrays.fill(start, NODES.length, NODES.length + 1, TRANSFORMS.get(NAMES[1]).transform(0.5));
    Arrays.fill(start, NODES.length + 1, 2 * NODES.length + 1, TRANSFORMS.get(NAMES[2]).transform(0.0));
    Arrays.fill(start, 2 * NODES.length + 1, 3 * NODES.length + 1, TRANSFORMS.get(NAMES[3]).transform(0.35));
    final DoubleMatrix1D vStart = new DoubleMatrix1D(start);

    final NonLinearLeastSquare ls = new NonLinearLeastSquare();
    final LeastSquareResults lsRes = ls.solve(new DoubleMatrix1D(MARKET_VOLS), new DoubleMatrix1D(SIGMA), func, jac, vStart);

    System.out.println("CapletStrippingMarketDataTest");
    System.out.println("chi2: " + lsRes.getChiSq() + "\n");

    final SABRTermStructureParameters sabrTS = (SABRTermStructureParameters) VOL_MODEL_PROVIDER.evaluate(lsRes.getFitParameters());
    // print the SABR curves
    final int nPoints = 101;
    System.out.println("t\talpha\tbeta\trho\tnu");
    for (int i = 0; i < nPoints; i++) {
      final double t = i * 10.0 / (nPoints - 1);
      System.out.println(t + "\t" + sabrTS.getAlpha(t) + "\t" + sabrTS.getBeta(t) + "\t" + sabrTS.getRho(t) + "\t" + sabrTS.getNu(t));
    }
    System.out.print("\n");

    // print the smile
    final int nStrikes = 21;
    for (final int element : CAP_EXPIRIES) {
      final int end = FREQUENCY * element;
      System.out.println(element + " year cap");
      for (int j = 0; j < nStrikes; j++) {
        final double k = 0.001 + j * 0.1 / (nStrikes - 1);
        final CapFloor cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, 1, end, k, true);
        final CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
        final double vol = pricer.impliedVol(sabrTS);
        System.out.println(k + "\t" + vol);
      }
      System.out.print("\n");
    }

  }

}
