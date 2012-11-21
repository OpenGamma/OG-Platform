/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.SoybeanFutureOptionExpiryCalculator;

/**
 *
 */
public class BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProviderTest {

  private static final String PREFIX = "S ";
  private static final String POSTFIX = "Comdty";
  private static final LocalDate DATE = LocalDate.of(2012, 11, 21);
  private static final Short[] EXPIRY_OFFSETS = new Short[] {1, 2, 8};
  private static final Double[] STRIKES = new Double[] {1350.0, 1400.0, 1450.0};

  private static final SoybeanFutureOptionExpiryCalculator EXPIRY_CALC = SoybeanFutureOptionExpiryCalculator.getInstance();
  @Test
  public void testUtils() {
    assertEquals(DATE.plusMonths(2), EXPIRY_CALC.getExpiryMonth(2, DATE));
    assertEquals(DATE.plusMonths(5), EXPIRY_CALC.getExpiryMonth(5, DATE));
  }
}
