/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class AnalyticOptionModelTest {
  protected static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> DUMMY_MODEL = new AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle>() {

    @Override
    public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
      return BSM.getPricingFunction(definition);
    }
  };
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry ONE_YEAR = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));

  private static final EuropeanVanillaOptionDefinition PUT = new EuropeanVanillaOptionDefinition(15, ONE_YEAR, false);
  private static final EuropeanVanillaOptionDefinition CALL = new EuropeanVanillaOptionDefinition(15, ONE_YEAR, true);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.06)), 0.02, new VolatilitySurface(ConstantDoublesSurface.from(0.24)),
      15., DATE);
  private static final double EPS = 1e-2;

  protected <S extends OptionDefinition, T extends StandardOptionDataBundle> void assertInputs(final AnalyticOptionModel<S, T> model, final S definition) {
    try {
      model.getPricingFunction(null);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }

    try {
      model.getPricingFunction(definition).evaluate((T) null);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testFiniteDifferenceAgainstBSM() {
    final Set<Greek> greekTypes = new HashSet<>(Greek.getAllGreeks());
    greekTypes.remove(Greek.DRIFTLESS_THETA);
    greekTypes.remove(Greek.STRIKE_DELTA);
    greekTypes.remove(Greek.STRIKE_GAMMA);
    greekTypes.remove(Greek.DUAL_DELTA);
    greekTypes.remove(Greek.DUAL_GAMMA);
    greekTypes.remove(Greek.ZETA);
    greekTypes.remove(Greek.ZETA_BLEED);
    GreekResultCollection bsm = BSM.getGreeks(PUT, DATA, greekTypes);
    GreekResultCollection finiteDifference = DUMMY_MODEL.getGreeks(PUT, DATA, greekTypes);
    assertResults(finiteDifference, bsm);
    bsm = BSM.getGreeks(CALL, DATA, greekTypes);
    finiteDifference = DUMMY_MODEL.getGreeks(CALL, DATA, greekTypes);
    assertResults(finiteDifference, bsm);
  }

  protected void assertResults(final GreekResultCollection results, final GreekResultCollection expected) {
    assertEquals(results.size(), expected.size());
    for (final Pair<Greek, Double> entry : results) {
      final Double result2 = expected.get(entry.getFirst());
      if (!(entry.getFirst().equals(Greek.VARIANCE_ULTIMA))) {
        assertEquals(entry.getSecond(), result2, EPS);
      } else {
        assertEquals(entry.getSecond(), result2, 1000 * EPS);
      }
    }
  }
}
