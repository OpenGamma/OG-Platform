/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * This contains market data for testing caplet stripping methods
 */
public abstract class CapletStrippingSetup {

  private static final double TINY = 1e-20;

  private static final double[] INDEX_CURVE_NODES = new double[] {0.0438356164383561, 0.0876712328767123,
    0.172602739726027, 0.254794520547945, 0.506849315068493, 0.758904109589041, 1.00547945205479, 2.01369863013698,
    3.01020285949547, 4.00547945205479, 5.00547945205479, 6.00547945205479, 7.01839958080694, 8.01095890410959,
    9.00821917808219, 10.0082191780821, 15.0074706190583, 20.0082191780821, 25.0109589041095, 30.0136986301369 };
  private static final double[] INDEX_CURVE_VALUES = new double[] {0.00184088091044285, 0.00201024117395892,
    0.00241264832694067, 0.00280755413825359, 0.0029541307818572, 0.00310125437814943, 0.00320054435838637,
    0.00377914611772073, 0.00483320020067661, 0.00654829256979543, 0.00877749583222556, 0.0112470678648412,
    0.0136301644164456, 0.0157618031582798, 0.0176836551757772, 0.0194174141169365, 0.0254011614777518,
    0.0282527762712854, 0.0298620063409043, 0.031116719228976 };

  private static final double[] DIS_CURVE_NODES = new double[] {0.00273972602739726, 0.0876712328767123,
    0.172602739726027, 0.254794520547945, 0.345205479452054, 0.424657534246575, 0.506849315068493, 0.758904109589041,
    1.00547945205479, 2.00547945205479, 3.01020285949547, 4.00547945205479, 5.00547945205479, 10.0054794520547 };

  private static final double[] DIS_CURVE_VALUES = new double[] {0.00212916045658802, 0.00144265912946933,
    0.00144567477491987, 0.00135441424749791, 0.00134009103595346, 0.00132773752749976, 0.00127592397233014,
    0.00132302501180961, 0.00138688847322639, 0.00172748279241698, 0.00254381216780551, 0.00410024606039574,
    0.00628782387356631, 0.0170033466745807 };

  private static final double[] CAP_STRIKES = new double[] {0.005, 0.01, 0.015, 0.02, 0.025, 0.03, 0.035, 0.04, 0.045,
    0.05, 0.055, 0.06, 0.07, 0.08, 0.09, 0.1, 0.11, 0.12 };
  private static final double[] CAP_STARTTIMES = new double[] {0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25 };
  private static final double[] CAP_ENDTIMES = new double[] {1, 2, 3, 4, 5, 7, 10 };

  // cal vol at each strike - NaN represents missing data
  private static final double[][] CAP_VOLS = new double[][] {
    {0.7175, 0.7781, 0.8366, Double.NaN, 0.8101, 0.7633, 0.714 },
    {Double.NaN, Double.NaN, 0.7523, 0.7056, 0.66095, 0.5933, 0.5313 },
    {Double.NaN, 0.78086, 0.73987, 0.667, 0.61469, 0.53502, 0.4691 },
    {Double.NaN, Double.NaN, Double.NaN, 0.63455, 0.56975, 0.48235, 0.4369 },
    {Double.NaN, 0.80496, 0.73408, 0.6081, 0.5472, 0.46445, 0.38854 },
    {Double.NaN, Double.NaN, Double.NaN, 0.61055, 0.52865, 0.432, 0.365 },
    {0.96976, 0.82268, 0.73761, Double.NaN, 0.5168, 0.4183, 0.35485 },
    {0.98927, 0.8376, 0.7274, 0.5983, 0.5169, 0.4083, 0.3375 },
    {1.00627, 0.83618, Double.NaN, 0.6003, Double.NaN, Double.NaN, 0.34498 },
    {1.02132, 0.8391, 0.7226, 0.5921, 0.4984, 0.3914, 0.32155 },
    {1.0255, 0.8406, 0.7196, 0.5962, 0.5035, 0.3873, 0.3227 },
    {1.0476, 0.8411, 0.7072, 0.58845, 0.50055, 0.3856, 0.3135 },
    {1.0467, Double.NaN, 0.70655, 0.5855, 0.50165, 0.3824, 0.3093 },
    {Double.NaN, Double.NaN, 0.70495, 0.58455, 0.499, 0.3802, 0.316 },
    {Double.NaN, 0.8458, 0.70345, 0.58335, 0.4984, 0.38905, 0.3164 },
    {Double.NaN, 0.8489, 0.70205, 0.58245, 0.4999, 0.39155, 0.3239 },
    {Double.NaN, Double.NaN, 0.7009, 0.5824, 0.5059, 0.4005, 0.3255 },
    {Double.NaN, Double.NaN, 0.6997, 0.5818, 0.5059, 0.4014, 0.3271 } };

  private static final double[] CAP_ATM_VOL = new double[] {0.69025, 0.753, 0.8284, 0.7907, 0.7074, 0.53703, 0.45421 };

  private static final Currency CUR = Currency.USD;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int FREQUENCY = 4;
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY,
      IS_EOM, "Ibor");

  private static final MulticurveProviderDiscount YIELD_CURVES;
  private static final MultiCapFloorPricer PRICER;

  static {

    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC);
    final InterpolatedDoublesCurve curve1 = new InterpolatedDoublesCurve(INDEX_CURVE_NODES, INDEX_CURVE_VALUES,
        interpolator, true);
    final InterpolatedDoublesCurve curve2 = new InterpolatedDoublesCurve(DIS_CURVE_NODES, DIS_CURVE_VALUES,
        interpolator, true);
    // single curve for discount and projection (this is consistent with Bloomberg's cap quotes)
    final YieldCurve indexCurve = YieldCurve.from(curve1);
    final YieldCurve disCurve = YieldCurve.from(curve2);
    // YIELD_CURVES = new YieldCurveBundle();
    YIELD_CURVES = new MulticurveProviderDiscount();
    YIELD_CURVES.setCurve(CUR, disCurve);
    YIELD_CURVES.setCurve(INDEX, indexCurve);
    PRICER = new MultiCapFloorPricer(getAllCaps(), YIELD_CURVES);
  }

  protected double[] getAtmVols() {
    return CAP_ATM_VOL;
  }

  protected static MulticurveProviderDiscount getYieldCurves() {
    return YIELD_CURVES;
  }

  static protected int getNumberOfStrikes() {
    return CAP_STRIKES.length;
  }

  static protected double[] getStrikes() {
    return CAP_STRIKES;
  }

  protected static double[] getCapStartTimes() {
    return CAP_STARTTIMES;
  }

  protected static double[] getCapEndTimes() {
    return CAP_ENDTIMES;
  }

  static protected IborIndex getIndex() {
    return INDEX;
  }

  protected static double[] getCapVols(final int strikeIndex) {

    final double[] vols = CAP_VOLS[strikeIndex];
    final int n = vols.length;
    final double[] temp = new double[n];
    int ii = 0;
    for (int i = 0; i < n; i++) {
      if (!Double.isNaN(vols[i])) {
        temp[ii++] = vols[i];
      }
    }
    final double[] res = new double[ii];
    System.arraycopy(temp, 0, res, 0, ii);
    return res;
  }

  protected static double[] getATMCapVols() {
    return CAP_ATM_VOL;
  }

  protected static double[] getAllCapVolsExATM() {
    final int nStrikes = CAP_STRIKES.length; // number of absolute strikes
    final int nTimes = CAP_ENDTIMES.length;
    final double[] temp = new double[nStrikes * nTimes];
    int jj = 0;
    for (int i = 0; i < nStrikes; i++) {
      final double[] v = getCapVols(i);
      final int n = v.length;
      System.arraycopy(v, 0, temp, jj, n);
      jj += n;
    }
    final double[] vols = new double[jj];
    System.arraycopy(temp, 0, vols, 0, jj);
    return vols;
  }

  protected static double[] getAllCapVols() {
    final int nStrikes = CAP_STRIKES.length; // number of absolute strikes
    final int nTimes = CAP_ENDTIMES.length;
    final double[] temp = new double[(nStrikes + 1) * nTimes];
    int jj = 0;
    for (int i = 0; i < nStrikes; i++) {
      final double[] v = getCapVols(i);
      final int n = v.length;
      System.arraycopy(v, 0, temp, jj, n);
      jj += n;
    }
    System.arraycopy(CAP_ATM_VOL, 0, temp, jj, nTimes);
    jj += nTimes;
    final double[] vols = new double[jj];
    System.arraycopy(temp, 0, vols, 0, jj);
    return vols;
  }

  protected static double[] getCapPrices(final int strikeIndex) {
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(strikeIndex), YIELD_CURVES);
    return pricer.price(getCapVols(strikeIndex));
  }

  protected static double[] getATMCapPrices() {
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getATMCaps(), YIELD_CURVES);
    return pricer.price(CAP_ATM_VOL);
  }

  protected static double[] getAllCapPrices() {
    return PRICER.price(getAllCapVols());
  }

  protected static List<CapFloor> getCaps(final int strikeIndex) {

    final double k = CAP_STRIKES[strikeIndex];
    final double[] vols = CAP_VOLS[strikeIndex];
    final int n = vols.length;
    final double[] temp = new double[n];
    int ii = 0;
    for (int i = 0; i < n; i++) {
      if (!Double.isNaN(vols[i])) {
        temp[ii++] = CAP_ENDTIMES[i];
      }
    }
    final double[] times = new double[ii];
    final double[] strikes = new double[ii];

    System.arraycopy(temp, 0, times, 0, ii);
    Arrays.fill(strikes, k);
    return makeCaps(strikes, times);
  }

  /**
   * Get the set of ATM caps. The strike (cap forward or swap rate) is determined from the know yield curves
   * @return set of ATM caps
   */
  protected static List<CapFloor> getATMCaps() {
    final int n = CAP_ENDTIMES.length;
    final double[] dummyK = new double[n];
    final List<CapFloor> dummyCaps = makeCaps(dummyK, CAP_ENDTIMES);
    final List<CapFloor> caps = new ArrayList<>(n);

    final Iterator<CapFloor> interator = dummyCaps.iterator();
    while (interator.hasNext()) {
      final CapFloor c = interator.next();
      final CapFloorPricer pricer = new CapFloorPricer(c, YIELD_CURVES);
      final double fwd = pricer.getCapForward();
      caps.add(c.withStrike(fwd));
    }
    return caps;
  }

  protected static List<CapFloor> getAllCaps() {
    final List<CapFloor> caps = getAllCapsExATM();
    caps.addAll(getATMCaps());
    return caps;
  }

  protected static List<CapFloor> getAllCapsExATM() {
    final int nStrikes = CAP_STRIKES.length; // number of absolute strikes
    final List<CapFloor> caps = new ArrayList<>((nStrikes + 1) * CAP_ENDTIMES.length);
    for (int i = 0; i < nStrikes; i++) {
      caps.addAll(getCaps(i));
    }
    return caps;
  }

  protected static List<CapFloor> makeCaps(final double[] strikes, final double[] capEndTime) {
    final int n = strikes.length;
    ArgumentChecker.isTrue(n == capEndTime.length, "stikes and capEndTime different length");
    final List<CapFloor> caps = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      final CapFloor cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, 1, (int) (FREQUENCY * capEndTime[i]), strikes[i],
          true);
      caps.add(cap);
    }
    return caps;
  }

  /**
   * Run a caplet stripper and test it against known results
   * @param stripper The caplet stripper
   * @param mktValues market value of the cap/floors
   * @param type Are the market values prices or volatilities
   * @param expectedResults The expected result
   * @param tol A machine tolerance to compare results
   */
  public void testStripping(CapletStripper stripper, double[] mktValues, MarketDataType type, double expChiSq,
      DoubleMatrix1D expFitParms, double tol, boolean print) {

    CapletStrippingResult results = stripper.solve(mktValues, type);
    if (print) {
      System.out.println(this.toString() + "testStripping");
      System.out.print(results);
    } else {
      assertEquals("chi2", expChiSq, results.getChiSq(), tol * expChiSq + TINY);
      AssertMatrix.assertEqualsVectors(expFitParms, results.getFitParameters(), tol);
    }
  }

  /**
   * Run a caplet stripper and test it against known results
   * @param stripper The caplet stripper
   * @param mktValues market value of the cap/floors
   * @param type Are the market values prices or volatilities
   * @param errors The errors use for a least-squares fit
   * @param expectedResults The expected result
   * @param tol A machine tolerance to compare results
   */
  public void testStripping(CapletStripper stripper, double[] mktValues, MarketDataType type, double[] errors,
      double expChiSq, DoubleMatrix1D expFitParms, double tol, boolean print) {

    CapletStrippingResult results = stripper.solve(mktValues, type, errors);
    if (print) {
      System.out.println(this.toString() + "testStripping");
      System.out.print(results);
    } else {
      assertEquals("chi2", expChiSq, results.getChiSq(), tol * expChiSq + TINY);
      AssertMatrix.assertEqualsVectors(expFitParms, results.getFitParameters(), tol);
    }
  }

  /**
   * Run a caplet stripper and test it against known results
   * @param stripper The caplet stripper
   * @param mktValues market value of the cap/floors
   * @param type Are the market values prices or volatilities
   * @param guess Starting guess of model parameters
   * @param expectedResults The expected result
   * @param tol A machine tolerance to compare results
   */
  public void testStripping(CapletStripper stripper, double[] mktValues, MarketDataType type, DoubleMatrix1D guess,
      double expChiSq, DoubleMatrix1D expFitParms, double tol, boolean print) {
    CapletStrippingResult results = stripper.solve(mktValues, type, guess);
    if (print) {
      System.out.println(this.toString() + "testStripping");
      System.out.print(results);
    } else {
      assertEquals("chi2", expChiSq, results.getChiSq(), tol * expChiSq + TINY);
      AssertMatrix.assertEqualsVectors(expFitParms, results.getFitParameters(), tol);
    }
  }

  /**
   * Run a caplet stripper and test it against known results
   * @param stripper The caplet stripper
   * @param mktValues market value of the cap/floors
   * @param type Are the market values prices or volatilities
   * @param errors The errors use for a least-squares fit
   * @param guess Starting guess of model parameters
   * @param expectedResults The expected result
   * @param tol A machine tolerance to compare results
   */
  public void testStripping(CapletStripper stripper, double[] mktValues, MarketDataType type, double[] errors,
      DoubleMatrix1D guess, double expChiSq, DoubleMatrix1D expFitParms, double tol, boolean print) {
    CapletStrippingResult results = stripper.solve(mktValues, type, errors, guess);
    if (print) {
      System.out.println(this.toString() + "testStripping");
      System.out.print(results);
    } else {
      assertEquals("chi2", expChiSq, results.getChiSq(), tol * expChiSq + TINY);
      AssertMatrix.assertEqualsVectors(expFitParms, results.getFitParameters(), tol);
    }
  }

  protected void compareFunc(final Function1D<DoubleMatrix1D, DoubleMatrix1D> func1,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> func2, final DoubleMatrix1D pos, final double tol) {
    final DoubleMatrix1D y1 = func1.evaluate(pos);
    final DoubleMatrix1D y2 = func2.evaluate(pos);
    AssertMatrix.assertEqualsVectors(y1, y2, tol);
  }

  protected void compareJacobianFunc(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jFunc1,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jFunc2, final DoubleMatrix1D pos, final double tol) {
    final DoubleMatrix2D jac1 = jFunc1.evaluate(pos);
    final DoubleMatrix2D jac2 = jFunc2.evaluate(pos);
    AssertMatrix.assertEqualsMatrix(jac1, jac2, tol);
  }

}
