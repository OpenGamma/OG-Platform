/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew.demo;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStripper;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStripperInterpolatedTermStructure;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStrippingResult;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CombinedCapletStrippingResults;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MarketDataType;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.MultiCapFloorPricer;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.SingleStrikeSetup;
import com.opengamma.util.test.TestGroup;

/**
 * Here we represent caplet volatilities at different expiries but a single strike by an interpolated curve (a term
 * structure). We solve each strike in turn by root finding for the curve(s) knot values. This will reproduce the 
 * market cap values exactly, however since there is no coupling across strikes, the resultant caplet volatility 
 * surface is highly non-smooth in the strike direction.    
 * 
 * 
 */
public class InterpolatedTermStructureDemo extends SingleStrikeSetup {

  /**
   * This fits each strike in turn (excluding the ATM) and combines the results into a caplet volatility surface   
   */
  @Test(groups = TestGroup.UNIT_SLOW)
  public void singleStrikeExATMTest() {

    final int n = getNumberOfStrikes();
    final CapletStrippingResult[] res = new CapletStrippingResult[n];
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());
      final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);

      res[i] = stripper.solve(getCapPrices(i), MarketDataType.PRICE);
      res[i].printCapletVols(System.out);
    }

    final CombinedCapletStrippingResults comRes = new CombinedCapletStrippingResults(res);
    comRes.printSurface(System.out, 101, 101);
  }

  /**
   * This is an inconsistent approach, which should NEVER be used in practice.Treating ATM caps like absolute strike
   * value caps produces a term structure (of volatilities of caplets belonging the ATM caps) that runs diagonally 
   * across the expiry-strike plane, with caplet volatilities difference to similar (in expiry and strike) volatilities 
   * from the absolute strike fitting; this then causes spikes when an (2D) interpolator is used to obtain a continuous
   * surface from the caplet volatilities.  
   */
  @Test(groups = TestGroup.UNIT_SLOW)
  public void singleStrikeSWithATMTest() {

    final int n = getNumberOfStrikes();
    final CapletStrippingResult[] res = new CapletStrippingResult[n + 1];
    for (int i = 0; i < n; i++) {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());
      final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);

      res[i] = stripper.solve(getCapPrices(i), MarketDataType.PRICE);
      res[i].printCapletVols(System.out);
    }
    {
      final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getATMCaps(), getYieldCurves());
      final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);

      res[n] = stripper.solve(getATMCapPrices(), MarketDataType.PRICE);
      res[n].printCapletVols(System.out);
    }

    final CombinedCapletStrippingResults comRes = new CombinedCapletStrippingResults(res);
    comRes.printSurface(System.out, 101, 101);
  }

}
