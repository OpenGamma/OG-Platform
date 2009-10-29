/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class BjerksundStenslandModelTest extends AnalyticOptionModelTest {
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry NINE_MONTHS = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.75));
  private static final Expiry TENTH_YEAR = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.1));
  private static final Expiry SIX_MONTHS = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> MODEL = new BjerksundStenslandModel();
  private static final double STRIKE = 100;
  private static final DiscountCurve CURVE = new ConstantInterestRateDiscountCurve(0.1);
  private static final double B = 0;
  private static final double SPOT1 = 90;
  private static final double SPOT2 = 100;
  private static final double SPOT3 = 110;
  private static final double SIGMA1 = 0.15;
  private static final double SIGMA2 = 0.25;
  private static final double SIGMA3 = 0.35;
  private static final List<Greek> GREEK_LIST = Arrays.asList(Greek.PRICE);
  private static final double EPS = 1e-4;

  @Test
  public void testInputs() {
    final AmericanVanillaOptionDefinition definition = new AmericanVanillaOptionDefinition(1., NINE_MONTHS, true);
    super.testInputs(MODEL, definition);
  }

  @Test
  public void test() {
    AmericanVanillaOptionDefinition call = new AmericanVanillaOptionDefinition(STRIKE, TENTH_YEAR, true);
    AmericanVanillaOptionDefinition put = new AmericanVanillaOptionDefinition(STRIKE, TENTH_YEAR, false);
    final VolatilitySurface surface = new ConstantVolatilitySurface(SIGMA1);
    StandardOptionDataBundle vars = new StandardOptionDataBundle(CURVE, B, surface, SPOT1, DATE);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 0.0205);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 1.8757);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 10);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA2)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 0.3151);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 3.1256);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 10.3725);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA3)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 0.9479);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 4.3746);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 11.1578);

    call = new AmericanVanillaOptionDefinition(STRIKE, SIX_MONTHS, true);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA1)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 0.8099);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 4.0628);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 10.7898);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA2)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 2.7180);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 6.7661);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 12.9814);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA3)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 4.9665);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 9.4608);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(call, vars, GREEK_LIST), 15.5137);

    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA1)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 10.0000);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 1.8757);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 0.0408);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA2)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 10.2280);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 3.1256);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 0.4552);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA3)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 10.8663);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 4.3746);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 1.2383);

    put = new AmericanVanillaOptionDefinition(STRIKE, SIX_MONTHS, false);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA1)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 10.54);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 4.0628);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 1.0689);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA2)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 12.4097);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 6.7661);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 3.2932);
    vars = vars.withVolatilitySurface(new ConstantVolatilitySurface(SIGMA3)).withSpot(SPOT1);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 14.6445);
    vars = vars.withSpot(SPOT2);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 9.4608);
    vars = vars.withSpot(SPOT3);
    testResult(MODEL.getGreeks(put, vars, GREEK_LIST), 5.8374);
  }

  private void testResult(final GreekResultCollection result, final double value) {
    assertEquals(((Double) result.get(Greek.PRICE).getResult()), value, EPS);
  }
}
