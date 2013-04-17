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

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquareWithPenalty;

/**
 * 
 */
public class CapletStrippingBootstrapTest extends CapletStrippingSetup {
  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();

  private static final double[] CAP_EXPIRIES = new double[] {1, 2, 3, 4, 5, 7, 10};
  // private static final double[] VOL_1PC = new double[] {0.9943, 0.7309, 0.7523, 0.7056, 0.661, 0.5933, 0.5313};
  private static final double[] VOL_1PC = new double[] {0.7145, 0.7561, 0.724, 0.73, 0.693, 0.6103, 0.5626};
  private static final double[] PRICE_1PC;
  private static final double STRIKE = 0.01;

  private static List<CapFloor> CAPS_1PC;

  // pSpline parameters
  private static BasisFunctionGenerator GEN = new BasisFunctionGenerator();
  private static NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static int N_KNOTS = 20;
  private static int DEGREE = 3;
  private static int DIFFERENCE_ORDER = 2;
  private static double LAMBDA = 1;
  private static DoubleMatrix2D PENALTY_MAT;
  private static List<Function1D<Double, Double>> B_SPLINES;
  private static Function1D<DoubleMatrix1D, DoubleMatrix1D> WEIGHTS_TO_SWAP_FUNC;

  static {
    final int n = CAP_EXPIRIES.length;
    CAPS_1PC = new ArrayList<>(n);
    PRICE_1PC = new double[n];
    for (int i = 0; i < n; i++) {
      CapFloor cap = SimpleCapFloorMaker.makeCap(CUR, INDEX, 1, (int) (FREQUENCY * CAP_EXPIRIES[i]), "funding", "3m Libor", STRIKE, true);
      CAPS_1PC.add(cap);
      CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
      PRICE_1PC[i] = pricer.price(VOL_1PC[i]);

      B_SPLINES = GEN.generateSet(0.0, CAP_EXPIRIES[CAP_EXPIRIES.length - 1], N_KNOTS, DEGREE);
      PSplineFitter psf = new PSplineFitter();
      final int nWeights = B_SPLINES.size();
      PENALTY_MAT = (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(nWeights, DIFFERENCE_ORDER), LAMBDA);
    }
  }

  @Test
  public void test() {

    final boolean print = false;
    if (print) {
      System.out.println("CapletStrippingBootstrapTest");
    }

    final CapletStrippingBootstrap bootstrap = new CapletStrippingBootstrap(CAPS_1PC, YIELD_CURVES);
    final double[] capletVols = bootstrap.capletVolsFromCapVols(VOL_1PC);

    if (print) {
      final int n = capletVols.length;
      System.out.println("caplet vols");
      for (int i = 0; i < n; i++) {
        System.out.println(capletVols[i]);
      }
      System.out.println();
    }

    final int n = CAP_EXPIRIES.length;

    VolatilityModel1D piecewise = new VolatilityModel1D() {

      @Override
      public Double getVolatility(double[] fwdKT) {
        return getVolatility(fwdKT[0], fwdKT[1], fwdKT[2]);
      }

      @Override
      public double getVolatility(SimpleOptionData option) {
        return getVolatility(0, 0, option.getTimeToExpiry());
      }

      @Override
      public double getVolatility(double forward, double strike, double timeToExpiry) {
        int index = Arrays.binarySearch(CAP_EXPIRIES, timeToExpiry);
        if (index >= 0) {
          if (index >= (n - 1)) {
            return capletVols[n - 1];
          }
          return capletVols[index + 1];
        } else if (index == -(n + 1)) {
          return capletVols[n - 1];
        } else {
          return capletVols[-index - 1];
        }
      }
    };

    // print the curve
    if (print) {
      System.out.println("caplet vol curve");
      for (int i = 0; i < 101; i++) {
        double t = i * 10. / 100;
        double sig = piecewise.getVolatility(0, 0, t);
        System.out.println(t + "\t" + sig);
      }
      System.out.println();
    }

    Iterator<CapFloor> iter = CAPS_1PC.iterator();
    int ii = 0;
    while (iter.hasNext()) {
      CapFloor cap = iter.next();
      CapFloorPricer pricer = new CapFloorPricer(cap, YIELD_CURVES);
      double vol = pricer.impliedVol(piecewise);
      if (print) {
        System.out.println(vol + "\t" + VOL_1PC[ii]);
      }
      assertEquals(vol, VOL_1PC[ii++], 1e-9);
    }

  }

  @Test(enabled=false)
  public void curveTest() {

    final Interpolator1D baseInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.STEP, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final ParameterLimitsTransform transform = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN); // alpha > 0
    // final ParameterLimitsTransform transform = new NullTransform();
    final TransformedInterpolator1D interpolator = new TransformedInterpolator1D(baseInterpolator, transform);
    final double[] knots = new double[] {0, 1, 2, 3, 4, 5, 7};
    final VolTermStructureModelProvider volModel = new VolTermStructureModelProvider(knots, interpolator);
    final CapletStrippingFunction func = new CapletStrippingFunction(CAPS_1PC, YIELD_CURVES, volModel);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> rootFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        DoubleMatrix1D capPrices = func.evaluate(x);
        double[] temp = capPrices.getData();
        final int n = temp.length;
        for (int i = 0; i < n; i++) {
          temp[i] -= VOL_1PC[i];
        }
        return new DoubleMatrix1D(temp);
      }
    };

    final NewtonDefaultVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder();
    DoubleMatrix1D res = rootFinder.getRoot(rootFunc, new DoubleMatrix1D(CAP_EXPIRIES.length, 0.1));
    System.out.println("knots");
    double[] fittedPoints = new double[CAP_EXPIRIES.length];
    for (int i = 0; i < CAP_EXPIRIES.length; i++) {
      fittedPoints[i] = transform.inverseTransform(res.getEntry(i));
      System.out.println(CAP_EXPIRIES[i] + "\t" + fittedPoints[i]);
    }
    System.out.println();

    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(knots, res.getData(), interpolator, true);
    for (int i = 0; i < 101; i++) {
      double t = i * 10.0 / 100;
      System.out.println(t + "\t" + curve.getYValue(t));
    }
    System.out.println();

    VolatilityModel1D vol = volModel.evaluate(res);
    for (int i = 0; i < CAP_EXPIRIES.length; i++) {
      CapFloorPricer pricer = new CapFloorPricer(CAPS_1PC.get(i), YIELD_CURVES);
      double iv = pricer.impliedVol(vol);
      System.out.println(VOL_1PC[i] + "\t" + iv);
    }

  }

  @Test(enabled=false)
  public void pSplineTest() {

  //this maps the b-spline weights into a VolatilityModel1D - in this case a term structure of (caplet) volatility 
    final VolatilityModelProvider volModel = new VolatilityModelProvider() {

      @Override
      public VolatilityModel1D evaluate(DoubleMatrix1D x) {
        final Function1D<Double, Double> func = new BasisFunctionAggregation<>(B_SPLINES, x.getData());
        return new VolatilityModel1D() {

          @Override
          public Double getVolatility(double[] fwdkT) {
            return func.evaluate(fwdkT[2]);
          }

          @Override
          public double getVolatility(SimpleOptionData option) {
            return func.evaluate(option.getTimeToExpiry());
          }

          @Override
          public double getVolatility(double forward, double strike, double timeToExpiry) {
            return func.evaluate(timeToExpiry);
          }
        };
      }
    };
    
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new CapletStrippingFunction(CAPS_1PC, YIELD_CURVES, volModel);
    int n = B_SPLINES.size();
    LeastSquareResults ans = NLLSWP.solve(new DoubleMatrix1D(VOL_1PC)                                         , func, new DoubleMatrix1D(n, 1.0),PENALTY_MAT);
   
    System.out.println(ans);

  }

}
