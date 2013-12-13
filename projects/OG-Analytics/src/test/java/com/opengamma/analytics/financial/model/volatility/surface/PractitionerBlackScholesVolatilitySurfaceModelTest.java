/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

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
public class PractitionerBlackScholesVolatilitySurfaceModelTest {
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final PractitionerBlackScholesVolatilitySurfaceModel MODEL = new PractitionerBlackScholesVolatilitySurfaceModel();
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.04));
  private static final double B = 0.03;
  private static final double SPOT = 100;
  private static final boolean IS_CALL = true;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry[] EXPIRY = new Expiry[] {new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25)), new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5)),
      new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.75)), new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1))};
  private static final double[] OFFSET = new double[] {0.05, 0.1, 0.125};
  private static final Expiry[] TEST_EXPIRY = new Expiry[] {new Expiry(DateUtils.getDateOffsetWithYearFraction(EXPIRY[0].getExpiry(), OFFSET[0])),
      new Expiry(DateUtils.getDateOffsetWithYearFraction(EXPIRY[1].getExpiry(), OFFSET[1])), new Expiry(DateUtils.getDateOffsetWithYearFraction(EXPIRY[2].getExpiry(), OFFSET[2]))};
  private static final double[] STRIKE = new double[] {80, 86, 100, 101, 110};
  private static final double[] TEST_STRIKE = new double[] {85, 95, 104};
  private static final double EPS = 1e-9;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPriceInput() {
    MODEL.getSurface(null, new StandardOptionDataBundle(null, 0, null, 0, null));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDataInput() {
    MODEL.getSurface(Collections.<OptionDefinition, Double> emptyMap(), null);
  }

  @Test
  public void testFlatSurface() {
    final Map<OptionDefinition, Double> prices = new HashMap<>();
    final double sigma = 0.3;
    OptionDefinition definition;
    final StandardOptionDataBundle data = new StandardOptionDataBundle(CURVE, B, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), SPOT, DATE);
    try {
      MODEL.getSurface(prices, data);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    for (final Expiry expiry : EXPIRY) {
      for (final double strike : STRIKE) {
        definition = new EuropeanVanillaOptionDefinition(strike, expiry, IS_CALL);
        prices.put(definition, BSM.getPricingFunction(definition).evaluate(data));
      }
    }
    final VolatilitySurface surface = MODEL.getSurface(prices, data);
    for (final Expiry expiry : TEST_EXPIRY) {
      for (final double strike : TEST_STRIKE) {
        assertEquals(surface.getVolatility(DoublesPair.of(DateUtils.getDifferenceInYears(DATE, expiry.getExpiry()), strike)), sigma, EPS);
      }
    }
  }

  @Test
  public void testUnifomlyVaryingSurface() {
    final Map<OptionDefinition, Double> prices = new HashMap<>();
    OptionDefinition definition;
    StandardOptionDataBundle data = new StandardOptionDataBundle(CURVE, B, null, SPOT, DATE);
    final double diff = 0.09;
    final double startSigma = 0.18;
    final double[] sigma = new double[] {startSigma, startSigma + diff, startSigma + 2 * diff, startSigma + 3 * diff};
    for (int i = 0; i < sigma.length; i++) {
      for (final double strike : STRIKE) {
        definition = new EuropeanVanillaOptionDefinition(strike, EXPIRY[i], IS_CALL);
        data = data.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(sigma[i])));
        prices.put(definition, BSM.getPricingFunction(definition).evaluate(data));
      }
    }
    final VolatilitySurface surface = MODEL.getSurface(prices, data);
    double result;
    Expiry expiry;
    for (int i = 0; i < TEST_EXPIRY.length; i++) {
      expiry = TEST_EXPIRY[i];
      result = sigma[i] + 4 * diff * DateUtils.getDifferenceInYears(EXPIRY[i].getExpiry(), expiry.getExpiry());
      for (final double strike : TEST_STRIKE) {
        assertEquals(surface.getVolatility(DoublesPair.of(DateUtils.getDifferenceInYears(DATE, expiry.getExpiry()), strike)), result, EPS);
      }
    }
  }
}
