/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.demo;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.BasisSplineVolatilityTermStructureProvider;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStripper;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStripperPSplineTermStructure;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingResult;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingSetup;
import com.opengamma.analytics.financial.interestrate.capletstripping.CombinedCapletStrippingResults;
import com.opengamma.analytics.financial.interestrate.capletstripping.MarketDataType;
import com.opengamma.analytics.financial.interestrate.capletstripping.MultiCapFloorPricer;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class CapletStrippingPSplineTermStrucutreDemo extends CapletStrippingSetup {

  @Test
  public void singleStrikeTest() {
    final BasisSplineVolatilityTermStructureProvider vtsp = new BasisSplineVolatilityTermStructureProvider(0, 10.0, 10, 2);
    final int size = vtsp.getNumModelParameters();
    final DoubleMatrix1D guess = new DoubleMatrix1D(size, 0.5);
    final double lambda = 1000;

    final int n = getNumberOfStrikes();
    final CapletStrippingResult[] res = new CapletStrippingResult[n];
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());

      final CapletStripper stripper = new CapletStripperPSplineTermStructure(pricer, vtsp, lambda);

      final double[] vols = getCapVols(i);
      final int nVols = vols.length;
      final double[] errors = new double[nVols];
      Arrays.fill(errors, 1e-4); //1bps
      res[i] = stripper.solve(vols, MarketDataType.VOL, errors, guess);
      res[i].printCapletVols(System.out);
    }

    final CombinedCapletStrippingResults comRes = new CombinedCapletStrippingResults(res);
    comRes.printSurface(System.out, 101, 101);
  }

}
