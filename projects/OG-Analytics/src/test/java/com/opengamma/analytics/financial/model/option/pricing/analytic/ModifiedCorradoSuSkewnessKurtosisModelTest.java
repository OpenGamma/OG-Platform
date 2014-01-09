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
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ModifiedCorradoSuSkewnessKurtosisModelTest {
  private static final AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> CORRADO_SU = new ModifiedCorradoSuSkewnessKurtosisModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.07));
  private static final double B = 0.07;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.35));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final SkewKurtosisOptionDataBundle NORMAL_DATA = new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, 0, 3);
  private static final OptionDefinition CALL_100 = new EuropeanVanillaOptionDefinition(100, EXPIRY, true);
  private static final OptionDefinition CALL_125 = new EuropeanVanillaOptionDefinition(125, EXPIRY, true);
  private static final OptionDefinition PUT_75 = new EuropeanVanillaOptionDefinition(75, EXPIRY, false);
  private static final OptionDefinition PUT_100 = new EuropeanVanillaOptionDefinition(100, EXPIRY, false);
  private static final double EPS = 1e-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    CORRADO_SU.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CORRADO_SU.getPricingFunction(CALL_100).evaluate((SkewKurtosisOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(BSM.getPricingFunction(CALL_100).evaluate(NORMAL_DATA), CORRADO_SU.getPricingFunction(CALL_100).evaluate(NORMAL_DATA), EPS);
    assertEquals(BSM.getPricingFunction(PUT_75).evaluate(NORMAL_DATA), CORRADO_SU.getPricingFunction(PUT_75).evaluate(NORMAL_DATA), EPS);
    final SkewKurtosisOptionDataBundle data = new SkewKurtosisOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, -0.2, 3);
    Function1D<SkewKurtosisOptionDataBundle, Double> f = CORRADO_SU.getPricingFunction(PUT_75);
    assertEquals(0.3103, f.evaluate(data), EPS);
    assertEquals(0.3186, f.evaluate(data.withSkew(-0.1).withKurtosis(3.5)), EPS);
    assertEquals(0.3267, f.evaluate(data.withSkew(0.).withKurtosis(4.)), EPS);
    assertEquals(0.3347, f.evaluate(data.withSkew(0.1).withKurtosis(4.5)), EPS);
    assertEquals(0.3427, f.evaluate(data.withSkew(0.2).withKurtosis(5.)), EPS);
    f = CORRADO_SU.getPricingFunction(PUT_100);
    assertEquals(6.0422, f.evaluate(data), EPS);
    assertEquals(5.9217, f.evaluate(data.withSkew(-0.1).withKurtosis(3.5)), EPS);
    assertEquals(5.8015, f.evaluate(data.withSkew(0.).withKurtosis(4.)), EPS);
    assertEquals(5.6814, f.evaluate(data.withSkew(0.1).withKurtosis(4.5)), EPS);
    assertEquals(5.5615, f.evaluate(data.withSkew(0.2).withKurtosis(5.)), EPS);
    f = CORRADO_SU.getPricingFunction(CALL_100);
    assertEquals(7.7770, f.evaluate(data), EPS);
    assertEquals(7.6565, f.evaluate(data.withSkew(-0.1).withKurtosis(3.5)), EPS);
    assertEquals(7.5363, f.evaluate(data.withSkew(0.).withKurtosis(4.)), EPS);
    assertEquals(7.4162, f.evaluate(data.withSkew(0.1).withKurtosis(4.5)), EPS);
    assertEquals(7.2963, f.evaluate(data.withSkew(0.2).withKurtosis(5.)), EPS);
    f = CORRADO_SU.getPricingFunction(CALL_125);
    assertEquals(0.9567, f.evaluate(data), EPS);
    assertEquals(1.1173, f.evaluate(data.withSkew(-0.1).withKurtosis(3.5)), EPS);
    assertEquals(1.2782, f.evaluate(data.withSkew(0.).withKurtosis(4.)), EPS);
    assertEquals(1.4391, f.evaluate(data.withSkew(0.1).withKurtosis(4.5)), EPS);
    assertEquals(1.6000, f.evaluate(data.withSkew(0.2).withKurtosis(5.)), EPS);

  }
}
