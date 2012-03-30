package com.opengamma.analytics.example.curveconstruction;

// @export "matrix-imports"

/// @export "imports"
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
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
import java.util.LinkedHashMap;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileWriter;
import org.json.simple.JSONObject;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.Exception;
import java.lang.Math;

public class CurveConstructionExample {

    // @export "matrixDemo"
    public static void matrixDemo(PrintStream out) {
        double[][] matrix_2 = identityMatrix(2);
        out.format("2x2 identity matrix:%n%s%n%n", Arrays.deepToString(matrix_2));

        double[][] matrix_4 = identityMatrix(4);
        out.format("4x4 identity matrix:%n%s%n%n", Arrays.deepToString(matrix_4));

        DoubleMatrix2D m = new DoubleMatrix2D(matrix_4);
        out.format("DoubleMatrix2D:%n%s%n%n", m.toString());
    }

    // @export "matrixMultiplyDemo"
    public static void matrixMultiplyDemo(PrintStream out) {
        double[][] matrix_4 = identityMatrix(4);
        DoubleMatrix2D m = new DoubleMatrix2D(matrix_4);

        double[] data_1d = {1.0,2.0,3.0,4.0};
        DoubleMatrix1D v = new DoubleMatrix1D(data_1d);

        ColtMatrixAlgebra colt = new ColtMatrixAlgebra();
        out.println(colt.multiply(m, v));
    }

    // @export "polyDerivativeDemo"
    public static RealPolynomialFunction1D getFunction() {
        double[] coefficients = {-125,75,-15,1};
        return new RealPolynomialFunction1D(coefficients);
    }

    public static void polyDerivativeDemo(PrintStream out) {
        RealPolynomialFunction1D f = getFunction();

        assert f.evaluate(5.0) == 0.0;

        RealPolynomialFunction1D d = f.derivative();
        double[] coefficients = d.getCoefficients();
        out.println(Arrays.toString(coefficients));
    }

    // @export "rootFindingDemo"
    public static void rootFindingDemo(PrintStream out) {
        RealPolynomialFunction1D f = getFunction();

        CubicRealRootFinder cubic = new CubicRealRootFinder();
        java.lang.Double[] roots = cubic.getRoots(f);
        out.println(Arrays.toString(roots));

        BrentSingleRootFinder brent = new BrentSingleRootFinder();
        java.lang.Double root = brent.getRoot(f,-10.0,10.0);
        out.println(root);

        try {
            out.println("Trying to call getRoot with arguments that don't bracket the root...");
            brent.getRoot(f, -1.0, 1.0);
        } catch (java.lang.IllegalArgumentException e) {
            out.println("IllegalArgumentException called");
        }
    }

    // @export "annuityDerivatives"
    public static void annuityDerivativeDemo(PrintStream out) {
        double [] paymentTimes = {t};
        boolean isPayer = false;
        YieldCurveBundle bundle = getBundle(y);
        AnnuityCouponFixed annuity = new AnnuityCouponFixed(ccy, paymentTimes, r, yieldCurveName, isPayer);
        double presentValue = presentValueCalculator.visit(annuity, bundle);
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
    static YieldCurve yieldCurve = new YieldCurve(new ConstantDoublesCurve(y));
    static ParRateCalculator parRateCalculator = ParRateCalculator.getInstance();

    public static YieldCurveBundle getBundle(double y) {
        YieldCurveBundle bundle = new YieldCurveBundle();
        bundle.setCurve(yieldCurveName, yieldCurve);
        return bundle;
    }
    static YieldCurveBundle bundle = getBundle(y);

    public static void interestRateDerivativeDemo(PrintStream out) {
        Cash loan = new Cash(ccy, 0.0, t, notional, r, t, yieldCurveName);

        // @export "interestRateDerivatives-presentValue"
        YieldCurveBundle bundle = getBundle(y);

        for (double i = 1.0; i < 3.0; i += 1) {
            out.format("Yield curve interest rate at %f: %f%n", i, yieldCurve.getInterestRate(i));
            out.format("Yield curve discount factor at %f: %f%n", i, yieldCurve.getDiscountFactor(i));
        }

        double presentValue = presentValueCalculator.visit(loan, bundle);
        out.format("Present value of loan using this yield curve bundle %f%n", presentValue);

        double zeroCouponDiscountFactor = yieldCurve.getDiscountFactor(t);
        double checkCalculation = notional * (zeroCouponDiscountFactor * (1 + r*t) - 1);
        out.format("Manually calculating value of loan gives %f%n", checkCalculation);
        assert (presentValue - checkCalculation) < 0.0001;

        double parRateManual = (Math.exp(y*t)-1)/t;
        out.format("Calculate par rate manually: %f%n", parRateManual);

        double parRate = parRateCalculator.visitCash(loan, bundle);
        out.format("Calculate par rate using ParRateCalculator: %f%n", parRate);

        assert (parRate - parRateManual) < 0.0001;
    }

    // @export "yield-points"
    // factory takes interpolator, left extrapolator, right extrapolator
    static CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator("NaturalCubicSpline","LinearExtrapolator","FlatExtrapolator");

    public static void yieldPoints(PrintStream out) {
        Currency ccy = Currency.EUR;
        String curveName = "onlyThis";
        int nMats = 10;
        double[] rates = {0.025, 0.02, 0.0225, 0.025, 0.03, 0.035, 0.04, 0.045, 0.04, 0.045};

        FixedCouponSwap[] marketInstruments;
        double[] marketValues = new double[nMats];

        for (int i = 0; i < nMats; i++) {
//            FixedCouponSwap swap = makeSwap(ccy, i, rates[i], curveName);
//            marketInstruments[i] = swap;
            marketValues[i] = 0.0; // By definition, on-market swaps have zero value.
        }

        LinkedHashMap mapCurveMatrix = new LinkedHashMap();
        LinkedHashMap mapCurveInterpolation = new LinkedHashMap();
        LinkedHashMap mapSensInterpolation = new LinkedHashMap();

        MultipleYieldCurveFinderDataBundle curveFinderDataBundle;


        BroydenVectorRootFinder rootFinder = new BroydenVectorRootFinder();

        double[] guessArray = new double[nMats];
        for (int i = 0; i < nMats; i++) {
            guessArray[i] = 0.01;
        }
        DoubleMatrix1D guess = new DoubleMatrix1D(guessArray);
    }

    public static void makeSwap(Currency ccy, int nYears, double rate, String curveName) {
        double[] semiAnnualPayments = new double[2*nYears];
        for (int i = 0; i <= 2*nYears; i++) {
           semiAnnualPayments[i] = 0.5*i;
        }
        double[] annualPayments = new double[nYears];
        for (int i = 0; i <= nYears; i++) {
           annualPayments[i] = i;
        }
    }

    // @export "identityMatrix"
    public static double[][] identityMatrix(int n) {
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++ ) {
            for (int j = 0; j < n; j++ ) { if (i == j) {
                    matrix[i][j] = 1;
                } else {
                    matrix[i][j] = 0;
                }
            }
        }
        return matrix;
    }
}
