/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.util.Pair;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class BlackScholesMertonImpliedVolatilitySurfaceModelTest {
  private static final VolatilitySurfaceModel<OptionDefinition, StandardOptionDataBundle> MODEL = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final double EPS = 1e-3;

  @Test
  public void testInputs() {
    try {
      MODEL.getSurface(null, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      MODEL.getSurface(Collections.<OptionDefinition, Double> emptyMap(), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(new EuropeanVanillaOptionDefinition(Math.random(), new Expiry(DATE), true), 2.3), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    boolean isCall;
    double spot, strike, b, price;
    Expiry expiry;
    DiscountCurve curve;
    EuropeanVanillaOptionDefinition definition;
    StandardOptionDataBundle initialData, data;
    double sigma = 0.01;
    for (int i = 0; i < 100; i++) {
      expiry = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, Math.random() * 2));
      sigma += 0.03;
      spot = 2 * Math.random() + 10;
      strike = 2 * Math.random() + 10;
      curve = new ConstantInterestRateDiscountCurve(Math.random() / 10);
      b = Math.random() / 20;
      isCall = Math.random() < 0.5 ? true : false;
      definition = new EuropeanVanillaOptionDefinition(strike, expiry, isCall);
      initialData = new StandardOptionDataBundle(curve, b, null, spot, DATE);
      data = new StandardOptionDataBundle(curve, b, new ConstantVolatilitySurface(sigma), spot, DATE);
      price = BSM.getPricingFunction(definition).evaluate(data);
      assertEquals(sigma, MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(definition, price), initialData).getVolatility(new Pair<Double, Double>(0., 0.)),
          EPS);
    }
  }
}
