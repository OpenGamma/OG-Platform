/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.id.ExternalId;

/**
 *
 */
public class BloombergCommodityFutureOptionForwardCurveInstrumentProviderTest extends FinancialTestBase {
  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  private static final String FUTURE_OPTION_PREFIX = "S ";
  private static final String POSTFIX = "Comdty";
  private static final String SCHEME = ExternalSchemes.BLOOMBERG_BUID_WEAK.getName();
  private static final String PREFIX = "S ";
  private static final BloombergCommodityFuturePriceCurveInstrumentProvider PROVIDER =
      new BloombergCommodityFuturePriceCurveInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME);

  private static final LocalDate DATE = LocalDate.of(2012, 11, 21);
  static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("a");

  private static final Short[] EXPIRY_OFFSETS = new Short[] {1, 2, 8, 10};

  private static final String[] RESULTS = new String[] {
      "S Z2 Comdty", "S F3 Comdty", "S U3 Comdty", "S F4 Comdty"
  };

  @Test
  public void testFuturePriceCurveInstrumentProvider() {
    for (int i = 0; i < EXPIRY_OFFSETS.length; i++) {
      final String expected = RESULTS[i];
      final ExternalId actual = PROVIDER.getInstrument(EXPIRY_OFFSETS[i], DATE);
      assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, actual.getScheme());
      assertEquals(expected, actual.getValue());
    }
  }

  @Test
  public void testCycle() {
    BloombergCommodityFuturePriceCurveInstrumentProvider provider = new BloombergCommodityFuturePriceCurveInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME);
    assertEquals(provider, cycleObject(BloombergCommodityFuturePriceCurveInstrumentProvider.class, provider));
    provider = new BloombergCommodityFuturePriceCurveInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, SCHEME);
    assertEquals(provider, cycleObject(BloombergCommodityFuturePriceCurveInstrumentProvider.class, provider));
  }

}
