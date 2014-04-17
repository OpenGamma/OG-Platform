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

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.ConstantElasticityOfVarianceModelDataBundle;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConstantElasticityOfVarianceBlackEquivalentVolatilitySurfaceModelTest {
  private static final double SPOT = 100;
  private static final double T = 0.25;
  private static final double B = 0;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, T));
  private static final double BETA = 0.5;
  private static final ConstantElasticityOfVarianceModelDataBundle DATA = new ConstantElasticityOfVarianceModelDataBundle(CURVE, B, new VolatilitySurface(ConstantDoublesSurface.from(0.005)), SPOT,
      DATE, BETA);
  private static final ConstantElasticityOfVarianceBlackEquivalentVolatilitySurfaceModel MODEL = new ConstantElasticityOfVarianceBlackEquivalentVolatilitySurfaceModel();
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOptionData() {
    MODEL.getSurface(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyOptionData() {
    MODEL.getSurface(Collections.<OptionDefinition, Double> emptyMap(), DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(new EuropeanVanillaOptionDefinition(100, EXPIRY, true), 0.2), null);
  }

  @Test
  public void test() {
    final double eps = 1e-4;
    OptionDefinition option = new EuropeanVanillaOptionDefinition(90, EXPIRY, true);
    ConstantElasticityOfVarianceModelDataBundle data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0)));
    VolatilitySurface blackEquivalent = MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(option, 0.), data);
    assertEquals(BSM.getPricingFunction(option).evaluate(data.withVolatilitySurface(blackEquivalent)), BSM.getPricingFunction(option).evaluate(data), 0);
    data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0.5)));
    blackEquivalent = MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(option, 0.), data);
    assertEquals(BSM.getPricingFunction(option).evaluate(data.withVolatilitySurface(blackEquivalent)), 9.7531, eps);
    data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(1)));
    option = new EuropeanVanillaOptionDefinition(95, EXPIRY, true);
    blackEquivalent = MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(option, 0.), data);
    assertEquals(BSM.getPricingFunction(option).evaluate(data.withVolatilitySurface(blackEquivalent)), 5.2678, eps);
    data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(2)));
    option = new EuropeanVanillaOptionDefinition(100, EXPIRY, true);
    blackEquivalent = MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(option, 0.), data);
    assertEquals(BSM.getPricingFunction(option).evaluate(data.withVolatilitySurface(blackEquivalent)), 3.8897, eps);
    data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(3)));
    option = new EuropeanVanillaOptionDefinition(105, EXPIRY, true);
    blackEquivalent = MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(option, 0.), data);
    assertEquals(BSM.getPricingFunction(option).evaluate(data.withVolatilitySurface(blackEquivalent)), 3.7832, eps);
    data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(4)));
    option = new EuropeanVanillaOptionDefinition(115, EXPIRY, true);
    blackEquivalent = MODEL.getSurface(Collections.<OptionDefinition, Double> singletonMap(option, 0.), data);
    assertEquals(BSM.getPricingFunction(option).evaluate(data.withVolatilitySurface(blackEquivalent)), 2.7613, eps);
  }
}
