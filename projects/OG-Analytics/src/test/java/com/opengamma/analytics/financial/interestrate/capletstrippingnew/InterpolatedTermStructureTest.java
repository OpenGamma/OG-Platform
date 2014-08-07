/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;

/**
 * 
 */
public class InterpolatedTermStructureTest extends SingleStrikeSetup {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final VectorFieldFirstOrderDifferentiator DIFF = new VectorFieldFirstOrderDifferentiator();
  private static final String DEFAULT_INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private static final String DEFAULT_EXTRAPOLATOR = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final ParameterLimitsTransform TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);

  @Test
  public void atmTest() {
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getATMCaps(), getYieldCurves());
    final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);
    testATMStripping(stripper, 0.0, null, 1e-15, true);

  }

  @Test
  public void singleStrikeTest() {

    final int n = getNumberOfStrikes();
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());
      final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);
      testSingleStrikeStripping(stripper, i, 0.0, null, 1e-15, false);
    }
  }

  @Test
  public void singleStrikeTest2() {

    final int n = getNumberOfStrikes();
    final CapletStrippingResult[] res = new CapletStrippingResult[n];
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());
      final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);

      res[i] = stripper.solve(getCapPrices(i), MarketDataType.PRICE);
    }

    final CombinedCapletStrippingResults comRes = new CombinedCapletStrippingResults(res);
    comRes.printSurface(System.out, 101, 101);
  }

  /**
   * Try to fit all cap with a volatility surface that has no strike . Clearly the global fit in this case will not be good.
   *  Fitting for price (weighted by vega) is quick is reasonably quick (4s), however fitting directly for vol, even when
   *  starting from the price solution is slow (extra 34s), hence it is commented out. 
   */
  @Test
  public void globalFitTest() {
    final double[] knots = new double[] {0.25, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer, knots);

    final double[] vols = getAllCapVols();
    final double[] prices = pricer.price(vols);
    final double[] vega = pricer.vega(vols);
    final int n = vols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-3);//10bps
    for (int i = 0; i < n; i++) {
      vega[i] *= errors[i];
    }

    final CapletStrippingResult res1 = stripper.solve(prices, MarketDataType.PRICE, vega);
    assertEquals(287962.8189949142, res1.getChiSq(), 1e-15);
    //System.out.println(res);

    //    final CapletStrippingResult res2 = stripper.solve(vols, MarketDataType.VOL, errors, res1.getFitParameters());
    //    assertEquals(331729.9643697031, res2.getChiSq(), 1e-15);
    // System.out.println(res2);
  }

  @Test
  public void functionTest() {
    final double[] knots = new double[] {0.25, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());

    final TransformedInterpolator1D interpolator = new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR), TRANSFORM);
    final DiscreteVolatilityFunctionProvider pro = new InterpolatedDiscreteVolatilityFunctionProvider(knots, interpolator);
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, pro);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = imp.getCapVolFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = imp.getCapVolJacobianFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFuncFD = DIFF.differentiate(func);

    final int n = knots.length;
    final DoubleMatrix1D pos = new DoubleMatrix1D(new double[] {-2.5988673306069283, -2.844982855827331, -1.5387984379786612, 0.5778158582378516, -2.5870212252861786, 3.9140436982407287,
      0.1076620649390847, -0.7147567540460562, -0.8433237847675432, 2.7681699173991703, -0.31027863733279304 });

    final int nSamples = 20;
    for (int run = 0; run < nSamples; run++) {
      for (int i = 0; i < n; i++) {
        pos.getData()[i] = 6 * RANDOM.nextDouble() - 3.0;
      }
      //  System.out.println(pos);
      // final DoubleMatrix1D capVols = func.evaluate(pos);
      //  System.out.println(capVols);
      compareJacobianFunc(jacFunc, jacFuncFD, pos, 1e-5);
    }
  }
}
