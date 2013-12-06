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
import com.opengamma.analytics.financial.model.option.definition.ComplexChooserOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
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
public class ComplexChooserOptionModelTest {
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final double B = 0.05;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.35));
  private static final double SPOT = 50;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double CHOOSE_TIME = 0.25;
  private static final double CALL_LIFE = 0.5;
  private static final double PUT_LIFE = 7. / 12;
  private static final Expiry CHOOSE_DATE = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, CHOOSE_TIME));
  private static final Expiry CALL_EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, CALL_LIFE));
  private static final Expiry PUT_EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, PUT_LIFE));
  private static final double CALL_STRIKE = 55;
  private static final double PUT_STRIKE = 48;
  @SuppressWarnings("unused")
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(CALL_STRIKE, CALL_EXPIRY, true);
  @SuppressWarnings("unused")
  private static final OptionDefinition PUT = new EuropeanVanillaOptionDefinition(PUT_STRIKE, PUT_EXPIRY, false);
  private static final ComplexChooserOptionDefinition CHOOSER = new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
  private static final AnalyticOptionModel<ComplexChooserOptionDefinition, StandardOptionDataBundle> MODEL = new ComplexChooserOptionModel();
  @SuppressWarnings("unused")
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(CHOOSER).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    // TODO test wrt BSM
    // final double spot = 2;
    // final StandardOptionDataBundle data = DATA.withSpot(spot);
    // final ComplexChooserOptionDefinition chooser = new ComplexChooserOptionDefinition(new Expiry(DATE), CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
    // assertEquals(MODEL.getPricingFunction(chooser).evaluate(data), BSM.getPricingFunction(
    // new EuropeanVanillaOptionDefinition(CALL_STRIKE, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, CALL_LIFE)), true)).evaluate(data), 0);
    assertEquals(MODEL.getPricingFunction(CHOOSER).evaluate(DATA), 6.0508, 1e-4);
  }
}
