/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class AnalyticOptionModelTest {
  private static final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> DUMMY_MODEL = new AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle>() {

    @Override
    public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
      return BSM.getPricingFunction(definition);
    }
  };
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry ONE_YEAR = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final EuropeanVanillaOptionDefinition PUT = new EuropeanVanillaOptionDefinition(100, ONE_YEAR, false);
  private static final EuropeanVanillaOptionDefinition CALL = new EuropeanVanillaOptionDefinition(100, ONE_YEAR, true);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.06), 0.02, new ConstantVolatilitySurface(0.24), 105.,
      DATE);
  private static final double EPS = 1e-2;

  public <S extends OptionDefinition<?>, T extends StandardOptionDataBundle> void testInputs(final AnalyticOptionModel<S, T> model, final S definition) {
    try {
      model.getPricingFunction(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }

    try {
      model.getPricingFunction(definition).evaluate((T) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testFiniteDifferenceAgainstBSM() {
    final List<Greek> greekTypes = Arrays.asList(Greek.PRICE, Greek.ZETA, Greek.CARRY_RHO, Greek.DELTA, Greek.DRIFTLESS_THETA, Greek.DZETA_DVOL, Greek.ELASTICITY, Greek.PHI,
        Greek.RHO, Greek.THETA, Greek.VARIANCE_VEGA, Greek.VEGA, Greek.VEGA_P, Greek.ZETA_BLEED, Greek.DDELTA_DVAR, Greek.DELTA_BLEED, Greek.GAMMA, Greek.GAMMA_P, Greek.VANNA,
        Greek.VARIANCE_VOMMA, Greek.VEGA_BLEED, Greek.VOMMA, Greek.VOMMA_P, Greek.DVANNA_DVOL, Greek.GAMMA_BLEED, Greek.GAMMA_P_BLEED, Greek.SPEED, Greek.SPEED_P, Greek.ULTIMA,
        Greek.VARIANCE_ULTIMA, Greek.ZOMMA, Greek.ZOMMA_P);
    final GreekResultCollection bsm = BSM.getGreeks(PUT, DATA, greekTypes);
    final GreekResultCollection finiteDifference = DUMMY_MODEL.getGreeks(PUT, DATA, greekTypes);
    System.out.println(bsm);
    System.out.println(finiteDifference);
  }

  protected void testResults(final GreekResultCollection results, final GreekResultCollection expected) {
    assertEquals(results.size(), expected.size());
    final Iterator<Map.Entry<Greek, GreekResult<?>>> iter1 = results.entrySet().iterator();
    final Iterator<Map.Entry<Greek, GreekResult<?>>> iter2 = expected.entrySet().iterator();
    while (iter1.hasNext()) {
      final Map.Entry<Greek, GreekResult<?>> entry1 = iter1.next();
      final Map.Entry<Greek, GreekResult<?>> entry2 = iter2.next();
      assertEquals(entry1.getKey(), entry2.getKey());
      if (entry1.getValue() instanceof SingleGreekResult) {
        assertTrue(entry2.getValue() instanceof SingleGreekResult);
        assertEquals(((SingleGreekResult) entry1.getValue()).getResult(), ((SingleGreekResult) entry2.getValue()).getResult(), EPS);
      }
    }
  }
}
