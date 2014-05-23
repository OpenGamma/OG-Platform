/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.surface.MixedLogNormalVolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class LocalVolPDEPricerTest {

  LocalVolPDEPricer PRICER = new LocalVolPDEPricer();

  @Test
  public void testFlat() {
    //generate a local volatility surface using a mixed log-normal density as this allows us to have an analytical option price that we can compare with the numerical value
    final double t = 1.5;
    final double spot = 100.0;
    final double r = 0.05;
    final double q = 0;
    final double b = r - q;
    final double k = 120.0;
    final double sigma = 0.4;
    final boolean isCall = true;
    final ForwardCurve fc = new ForwardCurve(spot, r);

    final LocalVolatilitySurfaceStrike lv = new LocalVolatilitySurfaceStrike(ConstantDoublesSurface.from(sigma));

    final int tN = 60;
    final int sN = 80 * tN;

    final double pdePrice = PRICER.price(spot, k, r, b, t, lv, true, false, sN, tN);
    final double price = Math.exp(-r * t) * BlackFormulaRepository.price(fc.getForward(t), k, t, sigma, isCall);

    //    System.out.println(price + "\t" + pdePrice);
    assertEquals(price, pdePrice, 1e-5 * price);
  }

  @Test
  //(enabled = false)
  public void test() {
    //generate a local volatility surface using a mixed log-normal density as this allows us to have an analytical option price that we can compare with the numerical value
    final double t = 1.5;
    final double spot = 100.0;
    final double r = 0.1;
    final double q = 0.07;
    final double b = r - q;
    final double k = 120.0;
    final boolean isCall = true;
    final ForwardCurve fc = new ForwardCurve(spot, b);
    final YieldAndDiscountCurve discountCurve = new YieldCurve("test", ConstantDoublesCurve.from(r));
    final double[] w = new double[] {0.7, 0.25, 0.05 };
    final double[] sigma = new double[] {0.3, 0.6, 1.0 };
    final double[] mu = new double[] {0.0, 0.3, -0.5 };

    final MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma, mu);
    final LocalVolatilitySurfaceStrike lv = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);

    //TODO relatively large grid needed for moderate accuracy
    final int tN = 400;
    final int sN = 2 * tN;

    final double pdePrice = PRICER.price(spot, k, r, b, t, lv, true, false, sN, tN);
    final double price = MixedLogNormalVolatilitySurface.getPriceSurface(fc, discountCurve, data).getPrice(t, k);
    final double vol = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data).getVolatility(t, k);
    final double price2 = Math.exp(-r * t) * BlackFormulaRepository.price(spot * Math.exp(b * t), k, t, vol, isCall);

    //System.out.println(price + "\t" + price2 + "\t" + pdePrice);
    assertEquals(price, pdePrice, 1e-3 * price);

  }
}
