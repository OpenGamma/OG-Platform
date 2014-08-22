/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static com.opengamma.analytics.financial.interestrate.capletstripping.SimpleCapFloorMaker.makeCap;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorPricerTest extends CapletStrippingSetup {

  private static final double s_flatVol = 0.45;
  private static final VolatilitySurface s_vs = new VolatilitySurface(new ConstantDoublesSurface(s_flatVol));

  @Test
  public void test() {
    IborIndex index = getIndex();
    Currency ccy = index.getCurrency();
    double strike = 0.003;
    //note - spot starting caps (floors) (usually) do not include the first caplet (floorlet), as the spot Libor is already 
    //known, so is not an option but a fixed cash-flow. Hence in makeCap have start = 1
    CapFloor cap = makeCap(ccy, index, 1, 20, strike, false);
    CapFloorPricer pricer = new CapFloorPricer(cap, getYieldCurves());

    //see comment above as to why there are 19 rather than 20 caplets 
    int nCaplets = 19;
    assertEquals(nCaplets, pricer.getNumberCaplets());

    assertEquals("strike", strike, pricer.getStrike());

    double[] expiries = pricer.getCapletExpiries();
    assertEquals(nCaplets, expiries.length);
    for (int i = 0; i < nCaplets; i++) {
      assertEquals(0.25 * (1 + i), expiries[i]);
    }

    SimpleOptionData[] options = pricer.getCapletAsOptionData();
    assertEquals(nCaplets, options.length);
    for (int i = 0; i < nCaplets; i++) {
      SimpleOptionData opt = options[i];
      assertEquals(strike, opt.getStrike());
      assertEquals(expiries[i], opt.getTimeToExpiry());
      assertEquals(false, opt.isCall());
    }

    //round trip test of cap volatility 
    int nVols = 10;
    for (int i = 0; i < nVols; i++) {
      double v = 0.1 + 0.6 * i / (nVols - 1.0);
      double p = pricer.price(v);
      assertEquals("cap vols", v, pricer.impliedVol(p), 1e-9);
    }

    //set all caplet vols to the same value 
    double vol = 0.37433;
    double[] capletVols = new double[nCaplets];
    Arrays.fill(capletVols, vol);
    double capPrice = pricer.price(capletVols);
    double capVol = pricer.impliedVol(capPrice);
    assertEquals(vol, capVol, 1e-9);
    capVol = pricer.impliedVol(capletVols);
    assertEquals(vol, capVol, 1e-11);

    double capVega = pricer.vega(vol);
    assertEquals("vega", capVega, pricer.vega(capletVols), 1e-12);

    //test volatility surface access
    assertEquals(s_flatVol, pricer.impliedVol(s_vs), 1e-13);
    assertEquals(pricer.price(s_flatVol), pricer.price(s_vs), 1e-15);
    assertEquals(pricer.vega(s_flatVol), pricer.vega(s_vs), 1e-15);
  }

}
