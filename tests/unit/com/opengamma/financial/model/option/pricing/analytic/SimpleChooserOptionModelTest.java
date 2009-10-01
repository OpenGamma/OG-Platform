/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.Price;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.option.definition.SimpleChooserOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class SimpleChooserOptionModelTest {
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final ZonedDateTime chooseDate = DateUtil.getDateOffsetWithYearFraction(date, 0.25);
    final Expiry underlyingExpiry = new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 0.5));
    final SimpleChooserOptionDefinition definition = new SimpleChooserOptionDefinition(50, underlyingExpiry, chooseDate);
    final StandardOptionDataBundle bundle = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.08), 0.08, new ConstantVolatilitySurface(0.25), 50., date);
    final AnalyticOptionModel<SimpleChooserOptionDefinition, StandardOptionDataBundle> model = new SimpleChooserOptionModel();
    assertEquals(model.getGreeks(definition, bundle, Arrays.asList(new Greek[] { new Price() })).values().iterator().next().values().iterator().next(), 6.1071, EPS);
  }
}
