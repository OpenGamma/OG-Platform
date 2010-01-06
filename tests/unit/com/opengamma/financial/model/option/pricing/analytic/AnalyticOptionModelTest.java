/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;
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
  protected static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> DUMMY_MODEL = new AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle>() {

    @Override
    public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
      return BSM.getPricingFunction(definition);
    }
  };
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry ONE_YEAR = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  private static final EuropeanVanillaOptionDefinition PUT = new EuropeanVanillaOptionDefinition(15, ONE_YEAR, false);
  private static final EuropeanVanillaOptionDefinition CALL = new EuropeanVanillaOptionDefinition(15, ONE_YEAR, true);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.06), 0.02, new ConstantVolatilitySurface(0.24), 15.,
      DATE);
  private static final double EPS = 1e-2;

  public <S extends OptionDefinition, T extends StandardOptionDataBundle> void testInputs(final AnalyticOptionModel<S, T> model, final S definition) {
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
    final List<Greek> greekTypes = Arrays.asList(Greek.PRICE, Greek.ZETA, Greek.CARRY_RHO, Greek.DELTA, Greek.DZETA_DVOL, Greek.ELASTICITY, Greek.PHI, Greek.RHO, Greek.THETA,
        Greek.VARIANCE_VEGA, Greek.VEGA, Greek.VEGA_P, Greek.ZETA_BLEED, Greek.VARIANCE_VANNA, Greek.DELTA_BLEED, Greek.GAMMA, Greek.GAMMA_P, Greek.VANNA, Greek.VARIANCE_VOMMA,
        Greek.VEGA_BLEED, Greek.VOMMA, Greek.VOMMA_P, Greek.DVANNA_DVOL, Greek.GAMMA_BLEED, Greek.GAMMA_P_BLEED, Greek.SPEED, Greek.SPEED_P, Greek.ULTIMA, Greek.VARIANCE_ULTIMA,
        Greek.ZOMMA, Greek.ZOMMA_P);
    GreekResultCollection bsm = BSM.getGreeks(PUT, DATA, greekTypes);
    GreekResultCollection finiteDifference = DUMMY_MODEL.getGreeks(PUT, DATA, greekTypes);
    testResults(finiteDifference, bsm);
    bsm = BSM.getGreeks(CALL, DATA, greekTypes);
    finiteDifference = DUMMY_MODEL.getGreeks(CALL, DATA, greekTypes);
    // testResults(finiteDifference, bsm);
  }

  protected void testResults(final GreekResultCollection results, final GreekResultCollection expected) {
    assertEquals(results.size(), expected.size());
    final Iterator<Map.Entry<Greek, GreekResult<?>>> iter = results.entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<Greek, GreekResult<?>> entry = iter.next();
      final GreekResult<?> result2 = expected.get(entry.getKey());
      if (entry.getValue() instanceof SingleGreekResult) {
        final Double first = ((SingleGreekResult) entry.getValue()).getResult();
        final Double second = ((SingleGreekResult) result2).getResult();
        if (!(entry.getKey().equals(Greek.VARIANCE_ULTIMA))) {
          assertEquals(first, second, EPS);
        } else {
          assertEquals(first / 1000, second / 1000, EPS);
        }
      }
    }
  }
}
