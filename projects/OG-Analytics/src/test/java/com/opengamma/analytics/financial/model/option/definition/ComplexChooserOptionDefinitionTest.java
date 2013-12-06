/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
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
public class ComplexChooserOptionDefinitionTest {
  private static final double CALL_STRIKE = 110;
  private static final double PUT_STRIKE = 90;
  private static final double DIFF = 50;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 6, 1);
  private static final Expiry CHOOSE_DATE = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final Expiry CALL_EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 2));
  private static final Expiry PUT_EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 3));
  private static final ComplexChooserOptionDefinition CHOOSER = new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
  private static final OptionDefinition VANILLA_CALL = new EuropeanVanillaOptionDefinition(CALL_STRIKE, CALL_EXPIRY, true);
  private static final OptionDefinition VANILLA_PUT = new EuropeanVanillaOptionDefinition(PUT_STRIKE, PUT_EXPIRY, false);
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> MODEL = new BlackScholesMertonModel();
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.06)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.15)),
      100., DATE);
  private static final Set<Greek> PRICE = Sets.newHashSet(Greek.FAIR_PRICE);
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiry() {
    new ComplexChooserOptionDefinition(null, CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCallStrike() {
    new ComplexChooserOptionDefinition(CHOOSE_DATE, -CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCallExpiry() {
    new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, null, PUT_STRIKE, PUT_EXPIRY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePutStrike() {
    new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, -PUT_STRIKE, PUT_EXPIRY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPutExpiry() {
    new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCallExpiry() {
    new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(CHOOSE_DATE.getExpiry(), -1)), PUT_STRIKE, PUT_EXPIRY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPutExpiry() {
    new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(CHOOSE_DATE.getExpiry(), -1)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCallExpiryTime() {
    CHOOSER.getTimeToCallExpiry(DateUtils.getDateOffsetWithYearFraction(DATE, 5));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPutExpiryTime() {
    CHOOSER.getTimeToPutExpiry(DateUtils.getDateOffsetWithYearFraction(DATE, 5));
  }

  @Test
  public void testGetters() {
    assertEquals(CHOOSER.getCallExpiry(), CALL_EXPIRY);
    assertEquals(CHOOSER.getCallStrike(), CALL_STRIKE, 0);
    assertEquals(CHOOSER.getPutExpiry(), PUT_EXPIRY);
    assertEquals(CHOOSER.getPutStrike(), PUT_STRIKE, 0);
    assertEquals(CHOOSER.getCallDefinition(), VANILLA_CALL);
    assertEquals(CHOOSER.getPutDefinition(), VANILLA_PUT);
    assertEquals(CHOOSER.getTimeToCallExpiry(DATE), 2, 0);
    assertEquals(CHOOSER.getTimeToPutExpiry(DATE), 3, 0);
  }

  @Test
  public void testEqualsAndHashCode() {
    OptionDefinition chooser = new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
    assertEquals(chooser, CHOOSER);
    assertEquals(chooser.hashCode(), CHOOSER.hashCode());
    chooser = new ComplexChooserOptionDefinition(new Expiry(DateUtils.getDateOffsetWithYearFraction(CHOOSE_DATE.getExpiry(), 0.5)), CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
    assertFalse(chooser.equals(CHOOSER));
    chooser = new ComplexChooserOptionDefinition(CHOOSE_DATE, PUT_STRIKE, CALL_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
    assertFalse(chooser.equals(CHOOSER));
    chooser = new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, PUT_EXPIRY, PUT_STRIKE, PUT_EXPIRY);
    assertFalse(chooser.equals(CHOOSER));
    chooser = new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, CALL_STRIKE, PUT_EXPIRY);
    assertFalse(chooser.equals(CHOOSER));
    chooser = new ComplexChooserOptionDefinition(CHOOSE_DATE, CALL_STRIKE, CALL_EXPIRY, PUT_STRIKE, CALL_EXPIRY);
    assertFalse(chooser.equals(CHOOSER));
  }

  @Test
  public void testPayoffFunction() {
    StandardOptionDataBundle data = DATA.withSpot(CALL_STRIKE + DIFF);
    final OptionPayoffFunction<StandardOptionDataBundle> payoff = CHOOSER.getPayoffFunction();
    assertEquals(MODEL.getGreeks(VANILLA_CALL, data, PRICE).get(Greek.FAIR_PRICE), payoff.getPayoff(data, null), EPS);
    data = DATA.withSpot(PUT_STRIKE - DIFF);
    assertEquals(MODEL.getGreeks(VANILLA_PUT, data, PRICE).get(Greek.FAIR_PRICE), payoff.getPayoff(data, null), EPS);
  }

  @Test
  public void testExerciseFunction() {
    final OptionExerciseFunction<StandardOptionDataBundle> exercise = CHOOSER.getExerciseFunction();
    exercise.shouldExercise(DATA.withSpot(CALL_STRIKE - DIFF), null);
    exercise.shouldExercise(DATA.withSpot(PUT_STRIKE + DIFF), null);
  }
}
