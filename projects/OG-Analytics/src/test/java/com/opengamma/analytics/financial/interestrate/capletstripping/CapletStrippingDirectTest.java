/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProviderDirect;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquareWithPenalty;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@SuppressWarnings("deprecation")
public class CapletStrippingDirectTest extends CapletStrippingSetup {

  private static final VectorFieldFirstOrderDifferentiator DIFF = new VectorFieldFirstOrderDifferentiator();
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  MatrixAlgebra MA = new OGMatrixAlgebra();

  /**
   * R White - This takes about 53s on my machine 
   * It takes 32 iterations of {@link  NonLinearLeastSquareWithPenalty} to converge 
   */
  @Test(groups = TestGroup.UNIT_SLOW)
  public void priceTest() {
    final double lambda = 0.03; //this is chosen to give a chi2/DoF of around 1 

    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    final double[] capVols = getAllCapVolsExATM();
    final double[] capPrices = pricer.price(capVols);
    final double[] capVega = pricer.vega(capVols);
    final int n = capVega.length;
    for (int i = 0; i < n; i++) {
      capVega[i] *= 1e-4; //this is approximately like having a 1bps error on volatility 
    }

    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    final DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getNumCaplets(), 0.7);

    final CapletStrippingResult res = stripper.solve(capPrices, MarketDataType.PRICE, capVega, guess);
    //  System.out.println(res);
    assertEquals(106.90175236602136, res.getChiSq(), 1e-15);
  }

  /**
   * R White - this takes about 20s on my machine
   * it takes 11 iterations of {@link  NonLinearLeastSquareWithPenalty} to converge 
   */
  @Test(groups = TestGroup.UNIT_SLOW)
  public void volTest() {
    final double lambda = 0.03; //this is chosen to give a chi2/DoF of around 1 
    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    final double[] capVols = getAllCapVolsExATM();
    final int n = capVols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); //1bps
    final DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    //System.out.println(res);
    assertEquals(106.90744994491888, res.getChiSq(), 1e-15);
  }

  @Test(groups = TestGroup.UNIT_SLOW)
  public void atmCapsVolTest() {
    final double lambda = 0.7; //this is chosen to give a chi2/DoF of around 1 
    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getATMCaps(), getYieldCurves());
    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    final double[] capVols = getATMCapVols();
    final int n = capVols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); //1bps
    final DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    //System.out.println(res);
    assertEquals(5.760490240384456, res.getChiSq(), 1e-15);
  }

  @Test(groups = TestGroup.UNIT_SLOW)
  public void allCapsVolTest() {
    final double lambdaT = 0.01; //this is chosen to give a chi2/DoF of around 1 
    final double lambdaK = 0.0002;
    final List<CapFloor> allCaps = getAllCaps();
    final List<CapFloor> atmCaps = getATMCaps();
    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(allCaps, getYieldCurves());
    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambdaK, lambdaT);

    final double[] capVols = getAllCapVols();
    final int n = capVols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-3); //10bps

    final int nATM = atmCaps.size();
    for (int i = 0; i < nATM; i++) {
      final int index = allCaps.indexOf(atmCaps.get(i));
      errors[index] = 1e-4; //1bps
    }

    final DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    // System.out.println(res);
    assertEquals(131.50826652583146, res.getChiSq(), 1e-15);

  }

  /**
   * We solve strike-by-strike (this takes 3-4 iterations), then concatenate the results as a starting
   * guess of a global fit; this doesn't make much different - converge in 9 rather than 11 iterations, 
   * to a slightly different point from above. 
   */
  @Test(groups = TestGroup.UNIT_SLOW)
  public void singleStrikeTest() {
    final double lambda = 0.03;
    DoubleMatrix1D guess = null;
    final int nStrikes = getNumberOfStrikes();
    final CapletStrippingResult[] singleStrikeResults = new CapletStrippingResult[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getCaps(i), getYieldCurves());
      final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

      final double[] capVols = getCapVols(i);
      final int n = capVols.length;
      final double[] errors = new double[n];
      Arrays.fill(errors, 1e-4); //1bps
      if (i == 0) {
        guess = new DoubleMatrix1D(pricer.getNumCaplets(), 0.7);
      }

      final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
      singleStrikeResults[i] = res;
      guess = res.getFitParameters();
      //  System.out.println(res);
    }

    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    final double[] capVols = getAllCapVolsExATM();
    final int n = capVols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); //1bps

    final double[] data = new double[pricer.getNumCaplets()];
    int pos = 0;
    for (int i = 0; i < nStrikes; i++) {
      final double[] ssData = singleStrikeResults[i].getFitParameters().getData();
      final int m = ssData.length;
      System.arraycopy(singleStrikeResults[i].getFitParameters().getData(), 0, data, pos, m);
      pos += m;
    }
    guess = new DoubleMatrix1D(data);
    // System.out.println(guess);
    final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    //System.out.println(res);
    assertEquals(106.90677987346953, res.getChiSq(), 1e-15);
  }

  @Test
  public void functionsTest() {

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCapsExATM(), getYieldCurves());
    final int size = pricer.getNumCaplets();
    final DoubleMatrix1D flat = new DoubleMatrix1D(size, 0.4);

    final DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderDirect(size);
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, volPro);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> pFunc = imp.getCapPriceFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFun = imp.getCapPriceJacobianFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFunFD = DIFF.differentiate(pFunc);
    compareJacobianFunc(pJacFun, pJacFunFD, flat, 1e-11);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> vFunc = imp.getCapVolFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFun = imp.getCapVolJacobianFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFunFD = DIFF.differentiate(vFunc);
    compareJacobianFunc(vJacFun, vJacFunFD, flat, 1e-6);

    //random entries
    //The FD calculation of the Jacobian takes a long time 
    final DoubleMatrix1D x = new DoubleMatrix1D(size, 0.0);
    int nRuns = 2;
    for (int run = 0; run < nRuns; run++) {
      for (int i = 0; i < size; i++) {
        x.getData()[i] = 0.2 + 0.7 * RANDOM.nextDouble();
      }
      compareJacobianFunc(pJacFun, pJacFunFD, x, 1e-11);
      compareJacobianFunc(vJacFun, vJacFunFD, x, 1e-6);
    }

    //test against CapletStrippingDirectGlobalWithPenalty

    final double[] capStrikes = pricer.getStrikes();
    final double[] capletExps = pricer.getCapletExpiries();
    final int nStrikes = capStrikes.length;

    @SuppressWarnings("unchecked")
    final List<CapFloor>[] caps = new List[nStrikes];
    final double[][] capVols = new double[nStrikes][];
    for (int i = 0; i < nStrikes; ++i) {
      caps[i] = getCaps(i);
      capVols[i] = getCapVols(i);
    }

    final CapletVolatilityNodalSurfaceProvider provider = new CapletVolatilityNodalSurfaceProvider(capStrikes, capletExps);
    final CapletStrippingDirectGlobalWithPenalty cpst = new CapletStrippingDirectGlobalWithPenalty(caps, getYieldCurves(), provider);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> vFuncOld = cpst.getCapVolFunc;
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFuncOld = cpst.getCapVolJacFunc;

    compareFunc(vFunc, vFuncOld, flat, 1e-10);
    compareJacobianFunc(vJacFun, vJacFuncOld, flat, 1e-10);
    nRuns = 50;
    for (int run = 0; run < nRuns; run++) {
      for (int i = 0; i < size; i++) {
        x.getData()[i] = 0.2 + 0.7 * RANDOM.nextDouble();
      }
      compareFunc(vFunc, vFuncOld, x, 1e-10);
      compareJacobianFunc(vJacFun, vJacFuncOld, x, 1e-10);
    }
  }

  @Test
  public void functionsTest2() {

    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCaps(), getYieldCurves());
    final int size = pricer.getGridSize();
    final DoubleMatrix1D flat = new DoubleMatrix1D(size, 0.4);

    final DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderDirect(size);
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, volPro);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> pFunc = imp.getCapPriceFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFun = imp.getCapPriceJacobianFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFunFD = DIFF.differentiate(pFunc);
    //    System.out.println(pJacFun.evaluate(flat));
    //    System.out.println(pJacFunFD.evaluate(flat));
    compareJacobianFunc(pJacFun, pJacFunFD, flat, 1e-11);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> vFunc = imp.getCapVolFunction();
    //   System.out.println(vFunc.evaluate(flat));

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFun = imp.getCapVolJacobianFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFunFD = DIFF.differentiate(vFunc);
    //    System.out.println(vJacFun.evaluate(flat));
    //    System.out.println(vJacFunFD.evaluate(flat));
    compareJacobianFunc(vJacFun, vJacFunFD, flat, 1e-6);

    //random entries
    //The FD calculation of the Jacobian takes a long time 
    final DoubleMatrix1D x = new DoubleMatrix1D(size, 0.0);
    final int nRuns = 2;
    for (int run = 0; run < nRuns; run++) {
      for (int i = 0; i < size; i++) {
        x.getData()[i] = 0.2 + 0.7 * RANDOM.nextDouble();
      }
      compareJacobianFunc(pJacFun, pJacFunFD, x, 1e-11);
      compareJacobianFunc(vJacFun, vJacFunFD, x, 1e-4);
      //            System.out.println(vJacFun.evaluate(x));
      //            System.out.println(vJacFunFD.evaluate(x));
    }
  }

  @Test
  public void timingTest() {
    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    final int size = pricer.getNumCaplets();
    final DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderDirect(size);
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, volPro);

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFun = imp.getCapVolJacobianFunction();
    final double[] capStrikes = getStrikes();
    final int nStrikes = capStrikes.length;

    @SuppressWarnings("unchecked")
    final List<CapFloor>[] caps = new List[nStrikes];
    final double[][] capVols = new double[nStrikes][];

    for (int i = 0; i < nStrikes; ++i) {
      caps[i] = getCaps(i);
      capVols[i] = getCapVols(i);
    }
    final CapletVolatilityNodalSurfaceProvider provider = new CapletVolatilityNodalSurfaceProvider(capStrikes, pricer.getCapletExpiries());
    final CapletStrippingDirectGlobalWithPenalty cpst = new CapletStrippingDirectGlobalWithPenalty(caps, getYieldCurves(), provider);

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFuncOld = cpst.getCapVolJacFunc;

    final int warmup = 5;
    final int hotspot = 30;

    //    final double t1 = funcTiming(size, vFunc, warmup, hotspot);
    //    final double t2 = funcTiming(size, vFuncOld, warmup, hotspot);
    final double t1 = jacTiming(size, vJacFun, warmup, hotspot);
    final double t2 = jacTiming(size, vJacFuncOld, warmup, hotspot);
    System.out.println(t1 + "s\t" + t2 + "s");
  }

  @SuppressWarnings("unused")
  private double funcTiming(final int nParms, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final int warmup, final int hotspot) {

    for (int i = 0; i < warmup; i++) {
      genFunc(nParms, func);
    }

    final long tStart = System.nanoTime();
    for (int i = 0; i < hotspot; i++) {
      genFunc(nParms, func);
    }
    final long tEnd = System.nanoTime();
    return (1e-9 * (tEnd - tStart)) / hotspot;
  }

  private double genFunc(final int nParms, final Function1D<DoubleMatrix1D, DoubleMatrix1D> func) {
    final DoubleMatrix1D x = new DoubleMatrix1D(new double[nParms]);
    final double[] data = x.getData();

    for (int i = 0; i < nParms; i++) {
      data[i] = 0.05 + RANDOM.nextDouble();
    }
    final DoubleMatrix1D y = func.evaluate(x);
    return y.getEntry(0) * 2.0;
  }

  private double jacTiming(final int nParms, final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc, final int warmup, final int hotspot) {

    for (int i = 0; i < warmup; i++) {
      genjac(nParms, jacFunc);
    }

    final long tStart = System.nanoTime();
    for (int i = 0; i < hotspot; i++) {
      genjac(nParms, jacFunc);
    }
    final long tEnd = System.nanoTime();
    return (1e-9 * (tEnd - tStart)) / hotspot;
  }

  private double genjac(final int nParms, final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc) {
    final DoubleMatrix1D x = new DoubleMatrix1D(new double[nParms]);
    final double[] data = x.getData();

    for (int i = 0; i < nParms; i++) {
      data[i] = RANDOM.nextDouble();
    }
    final DoubleMatrix2D jac = jacFunc.evaluate(x);
    return jac.getEntry(0, 0) * 2.0;
  }

}
