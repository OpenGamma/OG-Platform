/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static com.opengamma.web.analytics.blotter.BlotterColumn.MATURITY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.PRODUCT;
import static com.opengamma.web.analytics.blotter.BlotterColumn.QUANTITY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.RATE;
import static com.opengamma.web.analytics.blotter.BlotterColumn.TYPE;
import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BlotterColumnMappingsTest {

  private static final BlotterColumnMappings s_mappings =
      new BlotterColumnMappings(CurrencyPairs.of(ImmutableSet.of(CurrencyPair.of(Currency.GBP, Currency.USD))));

  /**
   * Simple security where fields are mapped using bean properties
   */
  @Test
  public void fra() {
    ExternalId regionId = ExternalId.of("Reg", "123");
    ExternalId underlyingId = ExternalId.of("Und", "321");
    ZonedDateTime startDate = ZonedDateTime.of(2012, 12, 21, 11, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime endDate = ZonedDateTime.of(2013, 12, 21, 11, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime fixingDate = ZonedDateTime.of(2013, 12, 20, 11, 0, 0, 0, TimeZone.UTC);
    FRASecurity security = new FRASecurity(Currency.AUD, regionId, startDate, endDate, 0.1, 1000, underlyingId, fixingDate);
    assertEquals("FRA", s_mappings.valueFor(TYPE, security));
    assertEquals(Currency.AUD, s_mappings.valueFor(PRODUCT, security));
    assertEquals(1000d, s_mappings.valueFor(QUANTITY, security));
  }

  /**
   * Custom providers for values derived from multiple security properties
   */
  @Test
  public void fxForward() {
    ZonedDateTime forwardDate = ZonedDateTime.of(2012, 12, 21, 11, 0, 0, 0, TimeZone.UTC);
    ExternalId regionId = ExternalId.of("Reg", "123");
    FXForwardSecurity security = new FXForwardSecurity(Currency.USD, 150, Currency.GBP, 100, forwardDate, regionId);
    assertEquals("FX Forward", s_mappings.valueFor(TYPE, security));
    assertEquals("GBP/USD FX Forward", s_mappings.valueFor(PRODUCT, security));
    assertEquals(forwardDate, s_mappings.valueFor(MATURITY, security));
    assertEquals("100.0/-150.0", s_mappings.valueFor(QUANTITY, security));
    assertEquals(1.5d, s_mappings.valueFor(RATE, security));
  }
}
