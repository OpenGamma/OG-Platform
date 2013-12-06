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
public class SimpleChooserOptionDefinitionTest {
  private static final double STRIKE = 100;
  private static final double DIFF = 20;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.1));
  private static final Expiry UNDERLYING_EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final SimpleChooserOptionDefinition CHOOSER = new SimpleChooserOptionDefinition(EXPIRY, STRIKE, UNDERLYING_EXPIRY);
  private static final OptionDefinition VANILLA_CALL = new EuropeanVanillaOptionDefinition(STRIKE, UNDERLYING_EXPIRY, true);
  private static final OptionDefinition VANILLA_PUT = new EuropeanVanillaOptionDefinition(STRIKE, UNDERLYING_EXPIRY, false);
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> MODEL = new BlackScholesMertonModel();
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.06)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.15)),
      STRIKE, DATE);
  private static final Set<Greek> PRICE = Sets.newHashSet(Greek.FAIR_PRICE);
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChooseDate() {
    new SimpleChooserOptionDefinition(EXPIRY, STRIKE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLateChooseDate() {
    new SimpleChooserOptionDefinition(EXPIRY, STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.05)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStrike() {
    new SimpleChooserOptionDefinition(EXPIRY, -STRIKE, UNDERLYING_EXPIRY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    CHOOSER.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void testExerciseFunction() {
    final OptionExerciseFunction<StandardOptionDataBundle> exercise = CHOOSER.getExerciseFunction();
    assertFalse(exercise.shouldExercise(DATA, STRIKE - DIFF));
    assertFalse(exercise.shouldExercise(DATA, STRIKE + DIFF));
  }

  @Test
  public void testPayoffFunction() {
    StandardOptionDataBundle data = DATA.withSpot(STRIKE + DIFF);
    final OptionPayoffFunction<StandardOptionDataBundle> payoff = CHOOSER.getPayoffFunction();
    assertEquals(MODEL.getGreeks(VANILLA_CALL, data, PRICE).get(Greek.FAIR_PRICE), payoff.getPayoff(data, null), EPS);
    data = DATA.withSpot(STRIKE - DIFF);
    assertEquals(MODEL.getGreeks(VANILLA_PUT, data, PRICE).get(Greek.FAIR_PRICE), payoff.getPayoff(data, null), EPS);
  }

  @Test
  public void testGetters() {
    assertEquals(CHOOSER.getCallDefinition(), VANILLA_CALL);
    assertEquals(CHOOSER.getPutDefinition(), VANILLA_PUT);
  }

  @Test
  public void testHashCodeAndEquals() {
    final SimpleChooserOptionDefinition definition1 = new SimpleChooserOptionDefinition(EXPIRY, STRIKE, UNDERLYING_EXPIRY);
    assertEquals(definition1, CHOOSER);
    assertEquals(definition1.hashCode(), CHOOSER.hashCode());
    final SimpleChooserOptionDefinition definition2 = new SimpleChooserOptionDefinition(new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5)), STRIKE, UNDERLYING_EXPIRY);
    final SimpleChooserOptionDefinition definition3 = new SimpleChooserOptionDefinition(EXPIRY, STRIKE + DIFF, UNDERLYING_EXPIRY);
    final SimpleChooserOptionDefinition definition4 = new SimpleChooserOptionDefinition(EXPIRY, STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.3)));
    assertFalse(CHOOSER.equals(definition2));
    assertFalse(CHOOSER.equals(definition3));
    assertFalse(CHOOSER.equals(definition4));
  }
}
