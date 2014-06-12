/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.statistics.descriptive.LognormalFisherKurtosisFromVolatilityCalculator;
import com.opengamma.analytics.math.statistics.descriptive.LognormalSkewnessFromVolatilityCalculator;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JarrowRuddSkewnessKurtosisModelTest {
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final double B = 0.02;
  private static final double SIGMA = 0.4;
  private static final double T = 0.5;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(SIGMA));
  private static final double SPOT = 90;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();
  private static final JarrowRuddSkewnessKurtosisModel MODEL = new JarrowRuddSkewnessKurtosisModel();
  private static final double SKEW;
  private static final double KURTOSIS;
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, T)), true);
  private static final OptionDefinition PUT = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, T)), false);
  private static final SkewKurtosisOptionDataBundle NORMAL_DATA;

  static {
    SKEW = new LognormalSkewnessFromVolatilityCalculator().evaluate(SIGMA, T);
    KURTOSIS = new LognormalFisherKurtosisFromVolatilityCalculator().evaluate(SIGMA, T) + 3;
    NORMAL_DATA = new SkewKurtosisOptionDataBundle(R, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(CALL).evaluate((SkewKurtosisOptionDataBundle) null);
  }

  @Test
  public void testNormal() {
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(NORMAL_DATA), BSM.getPricingFunction(CALL).evaluate(NORMAL_DATA), 1e-9);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(NORMAL_DATA), BSM.getPricingFunction(PUT).evaluate(NORMAL_DATA), 1e-9);
  }
}
