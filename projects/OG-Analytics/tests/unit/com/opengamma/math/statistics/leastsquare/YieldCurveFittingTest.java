/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.YieldCurveFittingFromSwapsTest;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class YieldCurveFittingTest extends YieldCurveFittingFromSwapsTest {

  public YieldCurveFittingTest() {
    _hotspotWarmupCycles = 0;
    _benchmarkCycles = 1;
  }

  protected YieldCurveFittingTestDataBundle getOverSpecifiedSetup() {

    List<String> curveNames = new ArrayList<String>();
    List<double[]> curveKnots = new ArrayList<double[]>();
    List<double[]> yields = new ArrayList<double[]>();

    String interpolatorName = Interpolator1DFactory.NATURAL_CUBIC_SPLINE;
    int[] payments = new int[] {1, 2, 3, 4, 5, 6, 8, 10, 12, 14, 20, 24, 30, 40, 50, 60};
    curveNames.add("single curve");
    curveKnots.add(new double[] {0.5, 1.00, 1.5, 2.005555556, 3.002777778, 4, 5, 7.008333333, 10, 15, 20.00277778, 25.00555556, 30.00555556});
    yields.add(new double[] {0.01, 0.015, 0.02, 0.03, 0.04, 0.05, 0.052, 0.049, 0.045, 0.044, 0.043, 0.041, 0.04});

    int n = curveKnots.get(0).length;
    final double[] rates = new double[n];
    for (int i = 0; i < n; i++) {
      rates[i] = 0.05;
    }
    DoubleMatrix1D startPosition = new DoubleMatrix1D(rates);

    ParRateCalculator calculator = ParRateCalculator.getInstance();
    ParRateCurveSensitivityCalculator sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();

    return getSwapOnlySetup(payments, curveNames, null, curveKnots, yields, startPosition, interpolatorName, calculator, sensitivityCalculator);
  }

  public void doHotSpot(YieldCurveFittingTestDataBundle data, final String name) {
    for (int i = 0; i < _hotspotWarmupCycles; i++) {
      doTest(data);
    }
    if (_benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(_logger, "processing {} cycles on " + name, _benchmarkCycles);
      for (int i = 0; i < _benchmarkCycles; i++) {
        doTest(data);
      }
      timer.finished();
    }
  }

  public void doTest(YieldCurveFittingTestDataBundle data) {
    Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());

    int n = data.getNumInstruments();
    DoubleMatrix1D y = new DoubleMatrix1D(new double[n]);
    DoubleMatrix1D sigma = new DoubleMatrix1D(new double[n]);
    for (int i = 0; i < n; i++) {
      sigma.getData()[i] = 1.0;
    }
    NonLinearLeastSquare ls = new NonLinearLeastSquare();
    LeastSquareResults res = ls.solve(y, sigma, func, jac, data.getStartPosition());

    assertEquals(0.0, res.getChiSq(), EPS);
    checkResult(res.getParameters(), data);
  }

  @Test
  public void testLevenbergMarquardt() {

    YieldCurveFittingTestDataBundle data = getSingleCurveSetup();
    doHotSpot(data, "Levenberg Marquardt (#nodes = #instruments)");

    data = getOverSpecifiedSetup();
    doHotSpot(data, "Levenberg Marquardt (#nodes < #instruments)");
  }
}
