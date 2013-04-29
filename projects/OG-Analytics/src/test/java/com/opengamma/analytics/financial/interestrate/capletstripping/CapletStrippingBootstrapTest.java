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

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructure;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function1DTest;
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
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.NullTransform;
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

  @Test
  public void test() {

    final boolean print = false;
    if (print) {
      System.out.println("CapletStrippingBootstrapTest");
    }
    final int nSamples = 101;
    final YieldCurveBundle yieldCurve = getYieldCurves();
    final int n = getNumberOfStrikes();

    final double[][] curve = new double[nSamples][n];

    for (int i = 0; i < n; i++) {
      List<CapFloor> caps = getCaps(i);
      double[] capVols = getCapVols(i);
      final CapletStrippingBootstrap bootstrap = new CapletStrippingBootstrap(caps, yieldCurve);
      final double[] capletVols = bootstrap.capletVolsFromCapVols(capVols);

      MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, yieldCurve);
      VolatilityTermStructure volCurve = getPiecewise(capletVols,bootstrap.getEndTimes());
      double[] fittedCapVols = pricer.impliedVols(volCurve);
      final int m = fittedCapVols.length;

      if (print) {
        for (int index = 0; index < nSamples; index++) {
          double t = index * 10.0 / (nSamples - 1);
          curve[index][i] = volCurve.getVolatility(t);
        }
      }

      for (int j = 0; j < m; j++) {
        assertEquals(i + "\t" + j, capVols[j], fittedCapVols[j], 2e-9);
      }

    }

    if (print) {
      System.out.print("\n");
      for (int index = 0; index < nSamples; index++) {
        double t = index * 10.0 / (nSamples - 1);
        System.out.print(t);
        for (int i = 0; i < n; i++) {
          System.out.print("\t" + curve[index][i]);
        }
        System.out.print("\n");
      }
    }

  }

  private VolatilityTermStructure getPiecewise(final double[] capletVols,  final double[] endTimes) {
    final int n = capletVols.length;
    Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double t) {
        int index = Arrays.binarySearch(endTimes, t);
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
    
    return new VolatilityCurve(FunctionalDoublesCurve.from(func));
  }

}
