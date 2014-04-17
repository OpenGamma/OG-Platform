/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.PiecewiseMixedLogNormalFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PiecewiseMixLogNormalFitterTest {

  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276 };
  private static final PiecewiseMixedLogNormalFitter FITTER = new PiecewiseMixedLogNormalFitter();
  private static final MixedLogNormalVolatilityFunction MODEL = MixedLogNormalVolatilityFunction.getInstance();

  @Test
  (enabled = false)
  public void test() {

    final MixedLogNormalModelData[] modelParms = FITTER.getFittedfModelParameters(FORWARD, STRIKES, EXPIRY, VOLS);
    final int n = modelParms.length;
    for (int i = 0; i < n; i++) {
      System.out.println(modelParms[i].toString());
    }

    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
  (enabled = false)
  public void bumpTest() {
    final double bump = 1e-2;
    final int index = 1;
    final double[] vols = Arrays.copyOf(VOLS, VOLS.length);
    vols[index] += bump;

    final MixedLogNormalModelData[] parms = FITTER.getFittedfModelParameters(FORWARD, STRIKES, EXPIRY, vols);
    for (final MixedLogNormalModelData parm : parms) {
      System.out.println(parm.toString());
    }
    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
  (enabled = false)
  public void flatTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);

    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      assertEquals(0.2, vol, 1e-9);
    }
  }

  @Test(enabled = false)
  public void badFitTest() {
    final double forward = 1.30276013603506;
    final double[] strikes = new double[] {1.080256504787705, 1.161299691076151, 1.329077636516407, 1.5210230159922162, 1.635211041136184 };
    final double expiry = 1.0;
    final double[] impVols = new double[] {0.2, 0.2, 0.2, 0.2, 0.2 };
    final double bump = 1e-3;
    final int index = 2;
    impVols[index] += bump;
    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(forward, strikes, expiry, impVols);

    for (int i = 0; i < 200; i++) {
      final double k = 0.8 + 1.2 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test(enabled = false)
  public void badFitTest2() {
    final double forward = 1172.011012;
    final double[] strikes = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
    final double expiry = 1.0;
    final double[] impVols = new double[] {0.2, 0.2, 0.2, 0.2, 0.2 };
    final double bump = 1e-3;
    final int index = 1;
    impVols[index] += bump;
    final double[] errors = new double[5];
    Arrays.fill(errors, 1e-4);
    errors[0] = 1e-1;
    errors[1] = 1e-1;
    final SmileModelFitter<MixedLogNormalModelData> fitter = new MixedLogNormalModelFitter(forward, strikes, expiry, impVols, errors, MODEL, 2, true);
    final DoubleMatrix1D start = new DoubleMatrix1D(0.15, 0.1, 0.5, 0.5);
    final LeastSquareResultsWithTransform lsres = fitter.solve(start);
    System.out.println(lsres.toString());
    final MixedLogNormalModelData data = new MixedLogNormalModelData(lsres.getModelParameters().getData());
    System.out.println(data.toString());
    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = MODEL.getVolatility(new EuropeanVanillaOption(k, expiry, true), forward, data);
      System.out.println(k + "\t" + vol);
    }
  }
}
