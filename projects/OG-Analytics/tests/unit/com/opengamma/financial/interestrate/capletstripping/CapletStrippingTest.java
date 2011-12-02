/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.SABRTermStructureParameters;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.volatility.VolatilityModel1D;
import com.opengamma.math.curve.AddCurveSpreadFunction;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.InterpolatedCurveBuildingFunction;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.curve.SpreadDoublesCurve;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.TransformedInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.money.Currency;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class CapletStrippingTest {

  private static final LinkedHashMap<String, Function1D<Double, Double>> PARAMETER_FUNCTIONS = new LinkedHashMap<String, Function1D<Double, Double>>();

  protected static final Currency CUR = Currency.USD;
  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  private static Function1D<Double, Double> ALPHA = new Function1D<Double, Double>() {

    private final double a = -0.01;
    private final double b = 0.05;
    private final double c = 0.3;
    private final double d = 0.04;

    @Override
    public Double evaluate(Double t) {
      return (a + t * b) * Math.exp(-c * t) + d;
    }
  };

  private static Function1D<Double, Double> BETA = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(Double t) {
      return 0.5;
    }
  };

  private static Function1D<Double, Double> RHO = new Function1D<Double, Double>() {

    private final double a = -0.7;
    private final double b = 0.0;
    private final double c = 0.1;
    private final double d = 0.0;

    @Override
    public Double evaluate(Double t) {
      return (a + t * b) * Math.exp(-c * t) + d;
    }
  };

  private static Function1D<Double, Double> NU = new Function1D<Double, Double>() {

    private final double a = 0.8;
    private final double b = 0.0;
    private final double c = 0.1;
    private final double d = 0.4;

    @Override
    public Double evaluate(Double t) {
      return (a + t * b) * Math.exp(-c * t) + d;
    }
  };

  protected static final Function1D<Double, Double> DISCOUNT_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.0375;
    private static final double b = 0.0021;
    private static final double c = 0.2;
    private static final double d = 0.04;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  protected static final Function1D<Double, Double> SPREAD_CURVE = new Function1D<Double, Double>() {

    private static final double a = 0.005;
    private static final double b = 0.002;
    private static final double c = 0.3;
    private static final double d = 0.001;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }
  };

  private static final LinkedHashMap<String, Curve<Double, Double>> CURVES;

  private static VolatilityModel1D VOL_MODEL;

  private static YieldCurveBundle YIELD_CURVES;
  private static List<CapFloor> CAPS;
  private static double[] MARKET_PRICES;
  private static double[] MARKET_VOLS;
  private static double[] MARKET_VEGAS;

  private static int[] CAP_MATURITIES = new int[] {1, 2, 3, 5, 10, 15, 20 };
  private static double[] NODES = new double[] {0., 1, 2, 3, 5, 10, 15, 20 };
  private static double[] STRIKES = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07, 0.1 };
  private static LinkedHashMap<String, double[]> CURVE_NODES;
  private static LinkedHashMap<String, Interpolator1D> INTERPOLATORS;
  private static LinkedHashMap<String, ParameterLimitsTransform> TRANSFORMS;
  private static final InterpolatedCurveBuildingFunction CURVE_BUILDER;
  private static String[] NAMES = new String[] {"alpha", "beta", "rho", "nu" };
  private static final DoubleMatrix1D START;
  private static final DoubleMatrix1D END;

  static {

    PARAMETER_FUNCTIONS.put(NAMES[0], ALPHA);
    PARAMETER_FUNCTIONS.put(NAMES[1], BETA);
    PARAMETER_FUNCTIONS.put(NAMES[2], RHO);
    PARAMETER_FUNCTIONS.put(NAMES[3], NU);

    TRANSFORMS = new LinkedHashMap<String, ParameterLimitsTransform>();
    TRANSFORMS.put(NAMES[0], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN)); //alpha > 0
    TRANSFORMS.put(NAMES[1], new DoubleRangeLimitTransform(0, 1)); //0<beta<1
    TRANSFORMS.put(NAMES[2], new DoubleRangeLimitTransform(-1, 1)); //-0.95<rho<0.95
    TRANSFORMS.put(NAMES[3], new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN));

    double[][] values = new double[4][NODES.length];
    for (int i = 0; i < 4; i++) {
      Function1D<Double, Double> func = PARAMETER_FUNCTIONS.get(NAMES[i]);
      ParameterLimitsTransform trans = TRANSFORMS.get(NAMES[i]);
      for (int j = 0; j < NODES.length; j++) {
        values[i][j] = trans.transform(func.evaluate(NODES[j])); //fitting parameters
      }
    }

    CURVES = new LinkedHashMap<String, Curve<Double, Double>>();
    CURVE_NODES = new LinkedHashMap<String, double[]>();
    INTERPOLATORS = new LinkedHashMap<String, Interpolator1D>();
    DoubleQuadraticInterpolator1D baseInterpolator = new DoubleQuadraticInterpolator1D();
    int index = 0;
    for (String name : NAMES) {
      CURVE_NODES.put(name, NODES);
      INTERPOLATORS.put(name, baseInterpolator);
      Interpolator1D intp = new TransformedInterpolator1D(baseInterpolator, TRANSFORMS.get(name));
      InterpolatedDoublesCurve intpCurve = InterpolatedDoublesCurve.from(NODES, values[index++], intp);
      CURVES.put(name, intpCurve);
    }

    VOL_MODEL = new SABRTermStructureParameters(CURVES.get(NAMES[0]), CURVES.get(NAMES[1]), CURVES.get(NAMES[2]), CURVES.get(NAMES[3]));

    YIELD_CURVES = new YieldCurveBundle();
    YIELD_CURVES.setCurve("funding", new YieldCurve(FunctionalDoublesCurve.from(DISCOUNT_CURVE)));
    YIELD_CURVES.setCurve("3m Libor", new YieldCurve(SpreadDoublesCurve.from(new AddCurveSpreadFunction(), FunctionalDoublesCurve.from(DISCOUNT_CURVE), FunctionalDoublesCurve.from(SPREAD_CURVE))));

    CAPS = new ArrayList<CapFloor>(CAP_MATURITIES.length * STRIKES.length);
    MARKET_PRICES = new double[CAP_MATURITIES.length * STRIKES.length];
    MARKET_VOLS = new double[CAP_MATURITIES.length * STRIKES.length];
    MARKET_VEGAS = new double[CAP_MATURITIES.length * STRIKES.length];

    int count = 0;
    for (int i = 0; i < CAP_MATURITIES.length; i++) {

      for (int j = 0; j < STRIKES.length; j++) {
        CapFloor cap = makeCap(CAP_MATURITIES[i], SimpleFrequency.QUARTERLY, "funding", "3m Libor", STRIKES[j]);
        CAPS.add(cap);
        CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
        MARKET_PRICES[count] = pricer.price(VOL_MODEL);
        MARKET_VOLS[count] = pricer.impliedVol(VOL_MODEL);
        MARKET_VEGAS[count] = 0.001 * pricer.vega(VOL_MODEL);
        count++;
      }
    }

    CURVE_BUILDER = new InterpolatedCurveBuildingFunction(CURVE_NODES, INTERPOLATORS);

    //start from some realistic values, and transform these into the fitting parameters
    double[] start = new double[4 * NODES.length];
    Arrays.fill(start, 0, NODES.length, TRANSFORMS.get(NAMES[0]).transform(0.3));
    Arrays.fill(start, NODES.length, 2 * NODES.length, TRANSFORMS.get(NAMES[1]).transform(0.7));
    Arrays.fill(start, 2 * NODES.length, 3 * NODES.length, TRANSFORMS.get(NAMES[2]).transform(-0.2));
    Arrays.fill(start, 3 * NODES.length, 4 * NODES.length, TRANSFORMS.get(NAMES[3]).transform(0.35));
    START = new DoubleMatrix1D(start);
    END = new DoubleMatrix1D(join(values));
  }

  private static double[] join(double[]... from) {
    int n = from.length;
    int sum = 0;
    for (int i = 0; i < n; i++) {
      sum += from[i].length;
    }
    double[] res = new double[sum];
    int index = 0;
    for (int i = 0; i < n; i++) {
      final int m = from[i].length;
      System.arraycopy(from[i], 0, res, index, m);
      index += m;
    }
    return res;
  }

  @Test
  public void testJacobian() {

    final VectorFieldFirstOrderDifferentiator jacFDCal = new VectorFieldFirstOrderDifferentiator();
    Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new CapletStrippingFunction(CAPS, YIELD_CURVES, CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFDFunc = jacFDCal.differentiate(func);
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacAnalFunc = new CapletStrippingJacobian(CAPS, YIELD_CURVES, CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);

    DoubleMatrix2D jacFD = jacFDFunc.evaluate(END);
    DoubleMatrix2D jacAnal = jacAnalFunc.evaluate(END);
    int rows = jacFD.getNumberOfRows();
    int cols = jacFD.getNumberOfColumns();

    assertEquals("# rows does not match", rows, jacAnal.getNumberOfRows(), 0);
    assertEquals("# cols does not match", cols, jacAnal.getNumberOfColumns(), 0);

    //    System.out.println(jacFD);
    //    System.out.println("");
    //    System.out.println(jacAnal);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        assertEquals("row: " + i + " cols: " + j, jacFD.getEntry(i, j), jacAnal.getEntry(i, j), 1e-6);
      }
    }
  }

  @Test
  public void testStripping() {

    CapletStrippingFunction func = new CapletStrippingFunction(CAPS, YIELD_CURVES, CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);
    CapletStrippingJacobian jac = new CapletStrippingJacobian(CAPS, YIELD_CURVES, CURVE_NODES, INTERPOLATORS, TRANSFORMS, null);

    double[] sigma = new double[MARKET_PRICES.length];
    Arrays.fill(sigma, 0.0001); //1bps

    NonLinearLeastSquare ls = new NonLinearLeastSquare();
    LeastSquareResults lsRes = ls.solve(new DoubleMatrix1D(MARKET_VOLS), new DoubleMatrix1D(sigma), func, jac, START);

    DoubleMatrix1D res = lsRes.getFitParameters();

    assertTrue("chi^2 too large", lsRes.getChiSq() < 0.3);

    //We don't recover exactly the initial curves. Why?
    LinkedHashMap<String, Curve<Double, Double>> curves = CURVE_BUILDER.evaluate(res);
    for (String name : NAMES) {
      Curve<Double, Double> fitCurve = curves.get(name);
      ParameterLimitsTransform trans = TRANSFORMS.get(name);
      Curve<Double, Double> initialCurve = CURVES.get(name);
      for (int i = 0; i < 25; i++) {
        double t = i * 20. / 25;
        assertEquals(name + " - time: " + t, initialCurve.getYValue(t), trans.inverseTransform(fitCurve.getYValue(t)), 5e-2);
      }
    }

  }

  private static CapFloor makeCap(final int years, final SimpleFrequency freq, final String discountCurve, final String indexCurve, final double strike) {
    int n = (int) (years * freq.getPeriodsPerYear()) - 1; //first caplet missing
    double tau = 1.0 / freq.getPeriodsPerYear();

    CapFloorIbor[] caplets = new CapFloorIbor[n];
    for (int i = 0; i < n; i++) {
      double fixingStart = (i + 1) * tau;
      double fixingEnd = (i + 2) * tau;
      caplets[i] = new CapFloorIbor(Currency.USD, fixingEnd, discountCurve, tau, 1.0, fixingStart, INDEX, fixingStart, fixingEnd, tau, indexCurve, strike, true);
    }
    return new CapFloor(caplets);
  }
}
