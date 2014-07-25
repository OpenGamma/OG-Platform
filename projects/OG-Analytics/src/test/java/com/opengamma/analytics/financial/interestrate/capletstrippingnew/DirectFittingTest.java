/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import org.testng.annotations.Test;

/**
 * 
 */
public class DirectFittingTest extends CapletStrippingSetup {

  @Test
  public void pricetest() {
    final CapletStripperDirect stripper = new CapletStripperDirect();
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCapsExATM(), getYieldCurves());

    final double[] capVols = getAllCapVolsExATM();
    final double[] capPrices = pricer.price(capVols);
    final CapletStrippingResult res = stripper.solveForPrice(pricer, capPrices);
    System.out.println(res);
  }

  @Test
  public void voltest() {
    final CapletStripperDirect stripper = new CapletStripperDirect();
    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCapsExATM(), getYieldCurves());

    final double[] capVols = getAllCapVolsExATM();

    final CapletStrippingResult res = stripper.solveForVol(pricer, capVols);
    System.out.println(res);
  }

}
