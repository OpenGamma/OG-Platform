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
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GramCharlierModelTest {
  private static final AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> GRAM_CHARLIER = new GramCharlierModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final double B = 0.05;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final double SPOT = 30;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 5. / 12));
  private static final double PERIODS_PER_YEAR = 12;
  private static final double SKEW = -2.3 / Math.sqrt(PERIODS_PER_YEAR);
  private static final double KURTOSIS = 1.2 / PERIODS_PER_YEAR;
  private static final SkewKurtosisOptionDataBundle NORMAL_DATA = new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, 0, 0);
  private static final SkewKurtosisOptionDataBundle DATA = new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS);
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(30, EXPIRY, true);
  private static final OptionDefinition PUT = new EuropeanVanillaOptionDefinition(30, EXPIRY, false);
  private static final double EPS = 1e-6;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    GRAM_CHARLIER.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    GRAM_CHARLIER.getPricingFunction(CALL).evaluate((SkewKurtosisOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(BSM.getPricingFunction(CALL).evaluate(NORMAL_DATA), GRAM_CHARLIER.getPricingFunction(CALL).evaluate(NORMAL_DATA), EPS);
    assertEquals(BSM.getPricingFunction(PUT).evaluate(NORMAL_DATA), GRAM_CHARLIER.getPricingFunction(PUT).evaluate(NORMAL_DATA), EPS);
    assertEquals(2.519585, GRAM_CHARLIER.getPricingFunction(CALL).evaluate(DATA), EPS);
    assertEquals(1.901050, GRAM_CHARLIER.getPricingFunction(PUT).evaluate(DATA), EPS);
  }
}
