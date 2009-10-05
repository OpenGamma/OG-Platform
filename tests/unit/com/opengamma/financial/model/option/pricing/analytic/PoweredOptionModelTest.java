/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.PoweredOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class PoweredOptionModelTest {
  private static final AnalyticOptionModel<PoweredOptionDefinition, StandardOptionDataBundle> POWERED_MODEL = new PoweredOptionModel();
  private static final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final List<Greek> REQUIRED_GREEKS = Arrays.asList(new Greek[] { Greek.PRICE });
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final DiscountCurve CURVE = new ConstantInterestRateDiscountCurve(0.1);
  private static final double B = 0.07;
  private static final double SPOT = 100;
  private static final double STRIKE = 100;
  private static final double SMALL_EPS = 1e-9;
  private static final double BIG_EPS = 1e-4;

  @Test
  public void testNonIntegerPower() {
    try {
      POWERED_MODEL.getGreeks(new PoweredOptionDefinition(100, EXPIRY, 1.3, true), null, REQUIRED_GREEKS);
      fail();
    } catch (OptionPricingException e) {
      // Expected
    }
  }

  @Test
  public void testPowerOfOne() {
    PoweredOptionDefinition poweredDefinition = getPoweredDefinition(1, true);
    EuropeanVanillaOptionDefinition vanillaDefinition = getVanillaOption(true);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.1);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.2);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.3);
    poweredDefinition = getPoweredDefinition(1, false);
    vanillaDefinition = getVanillaOption(false);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.1);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.2);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.3);
  }

  @Test
  public void test() {
    PoweredOptionDefinition definition = getPoweredDefinition(2, true);
    assertPriceEquals(definition, 0.1, 53.4487);
    assertPriceEquals(definition, 0.2, 160.2944);
    assertPriceEquals(definition, 0.3, 339.3713);
    definition = getPoweredDefinition(3, true);
    assertPriceEquals(definition, 0.1, 758.8427);
    assertPriceEquals(definition, 0.2, 4608.7213);
    assertPriceEquals(definition, 0.3, 15624.1041);
    definition = getPoweredDefinition(2, false);
    assertPriceEquals(definition, 0.1, 9.7580);
    assertPriceEquals(definition, 0.2, 57.8677);
    assertPriceEquals(definition, 0.3, 142.2726);
    definition = getPoweredDefinition(3, false);
    assertPriceEquals(definition, 0.1, 89.6287);
    assertPriceEquals(definition, 0.2, 1061.2120);
    assertPriceEquals(definition, 0.3, 3745.1853);
  }

  private void assertPriceEquals(PoweredOptionDefinition poweredDefinition, EuropeanVanillaOptionDefinition vanillaDefinition, double sigma) {
    StandardOptionDataBundle bundle = getBundle(sigma);
    GreekResultCollection actual = POWERED_MODEL.getGreeks(poweredDefinition, bundle, REQUIRED_GREEKS);
    GreekResultCollection expected = BSM.getGreeks(vanillaDefinition, bundle, REQUIRED_GREEKS);
    assertEquals(((SingleGreekResult)actual.get(Greek.PRICE)).getResult(), ((SingleGreekResult)expected.get(Greek.PRICE)).getResult(), SMALL_EPS);
  }

  private void assertPriceEquals(PoweredOptionDefinition poweredDefinition, double sigma, double price) {
    StandardOptionDataBundle bundle = getBundle(sigma);
    GreekResultCollection actual = POWERED_MODEL.getGreeks(poweredDefinition, bundle, REQUIRED_GREEKS);
    assertEquals(((SingleGreekResult)actual.get(Greek.PRICE)).getResult(), price, BIG_EPS * price);
  }

  private StandardOptionDataBundle getBundle(double sigma) {
    return new StandardOptionDataBundle(CURVE, B, new ConstantVolatilitySurface(sigma), SPOT, DATE);
  }

  private PoweredOptionDefinition getPoweredDefinition(double power, boolean isCall) {
    return new PoweredOptionDefinition(STRIKE, EXPIRY, power, isCall);
  }

  private EuropeanVanillaOptionDefinition getVanillaOption(boolean isCall) {
    return new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, isCall);
  }
}
