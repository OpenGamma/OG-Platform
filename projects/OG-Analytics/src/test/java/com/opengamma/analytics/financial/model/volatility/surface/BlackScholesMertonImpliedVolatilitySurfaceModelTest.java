/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlackScholesMertonImpliedVolatilitySurfaceModelTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final BlackScholesMertonImpliedVolatilitySurfaceModel MODEL = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.01)), 0.1, new VolatilitySurface(ConstantDoublesSurface.from(0.01)),
      100.,
      DateUtils.getUTCDate(2010, 1, 1));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final double EPS = 1e-3;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPrices() {
    MODEL.getSurface(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPrices() {
    MODEL.getSurface(Collections.<OptionDefinition, Double> emptyMap(), DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(new EuropeanVanillaOptionDefinition(RANDOM.nextDouble(), new Expiry(DATE), true), 2.3), null);
  }

  @Test
  public void test() {
    boolean isCall;
    double spot, strike, b, price;
    Expiry expiry;
    YieldAndDiscountCurve curve;
    EuropeanVanillaOptionDefinition definition;
    StandardOptionDataBundle initialData, data;
    double sigma = 0.01;
    for (int i = 0; i < 100; i++) {
      expiry = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, RANDOM.nextDouble() * 2));
      sigma += 0.03;
      spot = 2 * RANDOM.nextDouble() + 10;
      strike = 2 * RANDOM.nextDouble() + 10;
      curve = YieldCurve.from(ConstantDoublesCurve.from(RANDOM.nextDouble() / 10));
      b = 0;//RANDOM.nextDouble() / 20;
      isCall = RANDOM.nextDouble() < 0.5 ? true : false;
      definition = new EuropeanVanillaOptionDefinition(strike, expiry, isCall);
      initialData = new StandardOptionDataBundle(curve, b, null, spot, DATE);
      data = new StandardOptionDataBundle(curve, b, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), spot, DATE);
      price = BSM.getPricingFunction(definition).evaluate(data);
      assertEquals(sigma, MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(definition, price), initialData).getVolatility(DoublesPair.of(0., 0.)), EPS);
    }
  }
}
