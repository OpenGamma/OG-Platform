/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProviderDirect;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CapletStrippingResultTest extends CapletStrippingSetup {
  private static final MultiCapFloorPricer s_pricer;
  private static final DiscreteVolatilityFunction s_dvf;

  static {
    DiscreteVolatilityFunctionProviderDirect p = new DiscreteVolatilityFunctionProviderDirect();
    List<CapFloor> caps = getCaps(0);
    s_pricer = new MultiCapFloorPricer(caps, getYieldCurves());
    s_dvf = p.from(s_pricer.getExpiryStrikeArray());
  }

  @Test
  public void rootfindTest() {

    int nCaps = s_pricer.getNumCaps();
    int nCaplets = s_pricer.getNumCaplets();

    DoubleMatrix1D fitParms = new DoubleMatrix1D(nCaplets, 0.4);
    double[] prices = s_pricer.priceFromCapletVols(fitParms.getData());

    CapletStrippingResult results = new CapletStrippingResultRootFind(fitParms, s_dvf, s_pricer);

    AssertMatrix.assertEqualsVectors(fitParms, results.getCapletVols(), 1e-15);
    assertEquals(0.0, results.getChiSq());
    AssertMatrix.assertEqualsVectors(fitParms, results.getFitParameters(), 1e-15);
    double[] mCapPrices = results.getModelCapPrices();
    double[] mCapVols = results.getModelCapVols();
    assertEquals(nCaps, mCapVols.length);
    for (int i = 0; i < nCaps; i++) {
      assertEquals(prices[i], mCapPrices[i], 1e-14);
      assertEquals(0.4, mCapVols[i], 1e-9);
    }
  }

  @Test
  public void leastSquareTest() {
    int nCaps = s_pricer.getNumCaps();
    int nCaplets = s_pricer.getNumCaplets();
    DoubleMatrix1D fitParms = new DoubleMatrix1D(nCaplets, 0.4);
    IdentityMatrix cov = new IdentityMatrix(nCaplets);
    double chi2 = 0.456;
    double[] prices = s_pricer.priceFromCapletVols(fitParms.getData());

    LeastSquareResults res = new LeastSquareResults(chi2, fitParms, cov);
    CapletStrippingResult results = new CapletStrippingResultLeastSquare(res, s_dvf, s_pricer);
    AssertMatrix.assertEqualsVectors(fitParms, results.getCapletVols(), 1e-15);
    assertEquals(chi2, results.getChiSq());
    AssertMatrix.assertEqualsVectors(fitParms, results.getFitParameters(), 1e-15);
    double[] mCapPrices = results.getModelCapPrices();
    double[] mCapVols = results.getModelCapVols();
    assertEquals(nCaps, mCapVols.length);
    for (int i = 0; i < nCaps; i++) {
      assertEquals(prices[i], mCapPrices[i], 1e-14);
      assertEquals(0.4, mCapVols[i], 1e-9);
    }
  }
}
