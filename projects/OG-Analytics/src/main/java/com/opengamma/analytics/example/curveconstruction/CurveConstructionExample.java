/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

// @export "matrix-imports"

/// @export "imports"
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.CubicRealRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.money.Currency;

/**
 * Example for curve construction.
 */
public class CurveConstructionExample {
// CSOFF

  // @export "matrixDemo"
  public static void matrixDemo(final PrintStream out) {
    final double[][] matrix_2 = identityMatrix(2);
    out.format("2x2 identity matrix:%n%s%n%n", Arrays.deepToString(matrix_2));

    final double[][] matrix_4 = identityMatrix(4);
    out.format("4x4 identity matrix:%n%s%n%n", Arrays.deepToString(matrix_4));

    final DoubleMatrix2D m = new DoubleMatrix2D(matrix_4);
    out.format("DoubleMatrix2D:%n%s%n%n", m.toString());
  }

  // @export "matrixMultiplyDemo"
  public static void matrixMultiplyDemo(final PrintStream out) {
    final double[][] matrix_4 = identityMatrix(4);
    final DoubleMatrix2D m = new DoubleMatrix2D(matrix_4);

    final double[] data_1d = {1.0, 2.0, 3.0, 4.0 };
    final DoubleMatrix1D v = new DoubleMatrix1D(data_1d);

    final ColtMatrixAlgebra colt = new ColtMatrixAlgebra();
    out.println(colt.multiply(m, v));
  }

  // @export "polyDerivativeDemo"
  public static RealPolynomialFunction1D getFunction() {
    final double[] coefficients = {-125, 75, -15, 1 };
    return new RealPolynomialFunction1D(coefficients);
  }

  public static void polyDerivativeDemo(final PrintStream out) {
    final RealPolynomialFunction1D f = getFunction();

    assert f.evaluate(5.0) == 0.0;

    final RealPolynomialFunction1D d = f.derivative();
    final double[] coefficients = d.getCoefficients();
    out.println(Arrays.toString(coefficients));
  }

  // @export "rootFindingDemo"
  public static void rootFindingDemo(final PrintStream out) {
    final RealPolynomialFunction1D f = getFunction();

    final CubicRealRootFinder cubic = new CubicRealRootFinder();
    final java.lang.Double[] roots = cubic.getRoots(f);
    out.println(Arrays.toString(roots));

    final BrentSingleRootFinder brent = new BrentSingleRootFinder();
    final java.lang.Double root = brent.getRoot(f, -10.0, 10.0);
    out.println(root);

    try {
      out.println("Trying to call getRoot with arguments that don't bracket the root...");
      brent.getRoot(f, -1.0, 1.0);
    } catch (final java.lang.IllegalArgumentException e) {
      out.println("IllegalArgumentException called");
    }
  }

  // @export "annuityDerivatives"
  public static void annuityDerivativeDemo(final PrintStream out) {
    final double[] paymentTimes = {t };
    final boolean isPayer = false;
    final YieldCurveBundle bundle = getBundle(y);
    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(ccy, paymentTimes, r, yieldCurveName, isPayer);
    final double presentValue = annuity.accept(presentValueCalculator, bundle);
    out.format("Present value of 1-period annuity: %f%n", presentValue);
  }

  // @export "interestRateDerivatives"
  static Currency ccy = Currency.EUR;
  static double t = 1.0;
  static double r = 0.03;
  static double notional = 10000.0;
  static double y = 0.02;
  static int maturity = 5;
  static String yieldCurveName = "Euro Yield Curve Fixed 2%";
  static PresentValueCalculator presentValueCalculator = PresentValueCalculator.getInstance();
  static YieldCurve yieldCurve = YieldCurve.from(new ConstantDoublesCurve(y));
  static ParRateCalculator parRateCalculator = ParRateCalculator.getInstance();

  public static YieldCurveBundle getBundle(final double y) {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(yieldCurveName, yieldCurve);
    return bundle;
  }

  static YieldCurveBundle bundle = getBundle(y);

  public static void interestRateDerivativeDemo(final PrintStream out) {
    final Cash loan = new Cash(ccy, 0.0, t, notional, r, t, yieldCurveName);

    // @export "interestRateDerivatives-presentValue"
    final YieldCurveBundle bundle = getBundle(y);

    for (double i = 1.0; i < 3.0; i += 1) {
      out.format("Yield curve interest rate at %f: %f%n", i, yieldCurve.getInterestRate(i));
      out.format("Yield curve discount factor at %f: %f%n", i, yieldCurve.getDiscountFactor(i));
    }

    final double presentValue = loan.accept(presentValueCalculator, bundle);
    out.format("Present value of loan using this yield curve bundle %f%n", presentValue);

    final double zeroCouponDiscountFactor = yieldCurve.getDiscountFactor(t);
    final double checkCalculation = notional * (zeroCouponDiscountFactor * (1 + r * t) - 1);
    out.format("Manually calculating value of loan gives %f%n", checkCalculation);
    assert (presentValue - checkCalculation) < 0.0001;

    final double parRateManual = (Math.exp(y * t) - 1) / t;
    out.format("Calculate par rate manually: %f%n", parRateManual);

    final double parRate = parRateCalculator.visitCash(loan, bundle);
    out.format("Calculate par rate using ParRateCalculator: %f%n", parRate);

    assert (parRate - parRateManual) < 0.0001;
  }

  // @export "yield-points"
  // factory takes interpolator, left extrapolator, right extrapolator
  static CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator("NaturalCubicSpline", "LinearExtrapolator",
      "FlatExtrapolator");

  @SuppressWarnings({"unused", "rawtypes" })
  public static void yieldPoints(final PrintStream out) {
    final Currency currency = Currency.EUR;
    final String curveName = "onlyThis";
    final int nMats = 10;
    final double[] rates = {0.025, 0.02, 0.0225, 0.025, 0.03, 0.035, 0.04, 0.045, 0.04, 0.045 };

    final SwapFixedCoupon[] marketInstruments;
    final double[] marketValues = new double[nMats];

    for (int i = 0; i < nMats; i++) {
      //            FixedCouponSwap swap = makeSwap(ccy, i, rates[i], curveName);
      //            marketInstruments[i] = swap;
      marketValues[i] = 0.0; // By definition, on-market swaps have zero value.
    }

    final LinkedHashMap mapCurveMatrix = new LinkedHashMap();
    final LinkedHashMap mapCurveInterpolation = new LinkedHashMap();
    final LinkedHashMap mapSensInterpolation = new LinkedHashMap();

    final MultipleYieldCurveFinderDataBundle curveFinderDataBundle;

    final BroydenVectorRootFinder rootFinder = new BroydenVectorRootFinder();

    final double[] guessArray = new double[nMats];
    for (int i = 0; i < nMats; i++) {
      guessArray[i] = 0.01;
    }
    final DoubleMatrix1D guess = new DoubleMatrix1D(guessArray);
  }

  public static void makeSwap(final Currency ccy, final int nYears, final double rate, final String curveName) {
    final double[] semiAnnualPayments = new double[2 * nYears];
    for (int i = 0; i <= 2 * nYears; i++) {
      semiAnnualPayments[i] = 0.5 * i;
    }
    final double[] annualPayments = new double[nYears];
    for (int i = 0; i <= nYears; i++) {
      annualPayments[i] = i;
    }
  }

  // @export "identityMatrix"
  public static double[][] identityMatrix(final int n) {
    final double[][] matrix = new double[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i == j) {
          matrix[i][j] = 1;
        } else {
          matrix[i][j] = 0;
        }
      }
    }
    return matrix;
  }
}
