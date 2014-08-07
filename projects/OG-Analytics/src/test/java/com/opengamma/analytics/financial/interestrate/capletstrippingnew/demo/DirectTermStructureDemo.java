/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew.demo;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStripper;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStripperDirect;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStrippingResult;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CombinedCapletStrippingResults;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MarketDataType;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MultiCapFloorPricerGrid;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.SingleStrikeSetup;
import com.opengamma.util.test.TestGroup;

/**
 * Here we fit all the caplets with a common strike directly, applying a penalty on the curvature in the expiry direction.
 * We solve each strike in turn. This will reproduce the market cap values closely (depending on the value of the penalty
 * parameter, lambda), however since there is no coupling across strikes, the resultant caplet volatility 
 * surface is highly non-smooth in the strike direction.    
 * 
 * 
 */
public class DirectTermStructureDemo extends SingleStrikeSetup {

  /**
   * This fits each strike in turn (excluding the ATM) and combines the results into a caplet volatility surface   
   */
  @Test(groups = TestGroup.UNIT_SLOW)
  public void singleStrikeExATMTest() {
    final double lambda = 0.1; //this is chosen to recover the cap vols to better than 1bps 

    final int n = getNumberOfStrikes();
    final CapletStrippingResult[] res = new CapletStrippingResult[n];
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getCaps(i), getYieldCurves());
      final CapletStripper stripper = new CapletStripperDirect(pricer, lambda);
      final double[] vols = getCapVols(i);
      final int nVols = vols.length;
      final double[] errors = new double[nVols];
      Arrays.fill(errors, 1e-4); //1bps
      res[i] = stripper.solve(getCapVols(i), MarketDataType.VOL, errors);
      //System.out.println(res[i]);
      res[i].printCapletVols(System.out);
    }

    final CombinedCapletStrippingResults comRes = new CombinedCapletStrippingResults(res);
    comRes.printSurface(System.out, 101, 101);
  }
}
