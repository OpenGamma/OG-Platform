/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Arrays;

import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStripperDirect implements CapletStripper {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  @Override
  public CapletStrippingResult solveForPrice(final MultiCapFloorPricer pricer, final double[] capPrices) {

    final double lambdaT = 1.0;
    final double lambdaK = 1.0;

    final int nExp = pricer.getCapletExpiries().length;
    final int nStrikes = 18;
    final PSplineFitter pp = new PSplineFitter();
    final DoubleMatrix2D pTime = pp.getPenaltyMatrix(new int[] {nExp, nStrikes }, 2, 0);
    final DoubleMatrix2D pStrike = pp.getPenaltyMatrix(new int[] {nExp, nStrikes }, 2, 1);
    final DoubleMatrix2D p = (DoubleMatrix2D) MA.add(MA.scale(pTime, lambdaT), MA.scale(pStrike, lambdaK));

    final double[] capVols = pricer.impliedVols(capPrices);

    final double[] vega = pricer.vega(capVols);

    final CapletStrippingImp imp = getImp(pricer, capPrices);
    final CapletStrippingResult res = imp.solveForCapPrices(capPrices, vega, new DoubleMatrix1D(pricer.getNumCaplets(), 0.4), p);
    return res;
  }

  @Override
  public CapletStrippingResult solveForPrice(final MultiCapFloorPricer pricer, final double[] capPrices, final double[] errors) {
    return null;
  }

  @Override
  public CapletStrippingResult solveForVol(final MultiCapFloorPricer pricer, final double[] capVols) {

    final double lambdaT = 1.0;
    final double lambdaK = 1.0;

    final int nExp = pricer.getCapletExpiries().length;
    final int nStrikes = 18;
    final PSplineFitter pp = new PSplineFitter();
    final DoubleMatrix2D pTime = pp.getPenaltyMatrix(new int[] {nExp, nStrikes }, 2, 0);
    final DoubleMatrix2D pStrike = pp.getPenaltyMatrix(new int[] {nExp, nStrikes }, 2, 1);
    final DoubleMatrix2D p = (DoubleMatrix2D) MA.add(MA.scale(pTime, lambdaT), MA.scale(pStrike, lambdaK));

    final double[] error = new double[capVols.length];
    Arrays.fill(error, 1e-4);

    final CapletStrippingImp imp = getImp(pricer, capVols);
    final CapletStrippingResult res = imp.solveForCapVols(capVols, error, new DoubleMatrix1D(pricer.getNumCaplets(), 0.4), p);
    return res;
  }

  private CapletStrippingImp getImp(final MultiCapFloorPricer pricer, final double[] values) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notEmpty(values, "values");
    final int nCaps = pricer.getNumCaps();
    ArgumentChecker.isTrue(nCaps == values.length, "Expected {} cap prices, but only given {}", nCaps, values.length);
    final DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderDirect(pricer.getNumCaplets());
    return new CapletStrippingImp(pricer, volPro);
  }

}
