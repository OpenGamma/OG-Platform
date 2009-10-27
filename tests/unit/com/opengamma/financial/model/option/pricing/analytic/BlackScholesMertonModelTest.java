/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class BlackScholesMertonModelTest {
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final DiscountCurve CURVE;
  private static final Expiry ONE_YEAR = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  private static final Expiry NINE_MONTHS = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.75));
  private static final Expiry SIX_MONTHS = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final Expiry THREE_MONTHS = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final BlackScholesMertonModel MODEL = new BlackScholesMertonModel();
  private static final double EPS = 1e-4;

  static {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    data.put(0.25, 0.09);
    data.put(1.5, 0.09);
    data.put(2.5, 0.1);
    data.put(3.5, 0.05);
    CURVE = new DiscountCurve(data, new LinearInterpolator1D());
  }

  @Test
  public void testModels() {
    testPrice(65, THREE_MONTHS, true, 0.08, 0.08, 0.3, 60, 2.1334);
    testPrice(95, SIX_MONTHS, false, 0.1, 0.05, 0.2, 100, 2.4648);
    testPrice(19, NINE_MONTHS, true, 0.1, 0, 0.28, 19, 1.7011);
    testPrice(19, NINE_MONTHS, false, 0.1, 0, 0.28, 19, 1.7011);
    testPrice(3800, NINE_MONTHS, false, 0, 0, 0.15, 4200, 65.6185);
    testPrice(1.6, SIX_MONTHS, true, 0.06, -0.02, 0.12, 1.56, 0.0291);
  }

  @Test
  public void testGreeks() {

  }

  private void testPrice(final double strike, final Expiry expiry, final boolean isCall, final double r, final double b, final double sigma, final double spot, final double price) {
    final List<Greek> priceGreek = Arrays.asList(Greek.PRICE);
    final EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(strike, expiry, isCall);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(r), b, new ConstantVolatilitySurface(sigma), spot, DATE);
    final GreekResultCollection result = MODEL.getGreeks(definition, data, priceGreek);
    testResults(Greek.PRICE, result, price);
  }

  private void testResults(final Greek greek, final GreekResultCollection results, final double value) {
    final GreekResult<?> greekResult = results.get(greek);
    if (!greekResult.isMultiValued()) {
      testSingleResult(greekResult, value);
    }
  }

  private void testSingleResult(final GreekResult<?> singleResult, final double value) {
    final Double result = (Double) singleResult.getResult();
    assertEquals(result, value, EPS);
  }
}
