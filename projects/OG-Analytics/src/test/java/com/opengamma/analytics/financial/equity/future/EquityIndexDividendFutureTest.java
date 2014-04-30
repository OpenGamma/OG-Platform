/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexDividendFutureTest {

  private static final double PRICE = 95.0;
  private static final double timeToSettlement = 1.45;
  private static final double timeToFixing = 1.44;

  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime[] FIXING_DATES = {FIXING_DATE, FIXING_DATE.plusYears(1), DateUtils.getDateOffsetWithYearFraction(FIXING_DATE, 1.0) };
  private static final double[] FIXINGS = {98d, 99., 100.0 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.of(FIXING_DATES, FIXINGS, ZoneOffset.UTC);

  @Test
  public void test() {

    final EquityIndexDividendFuture theFuture = new EquityIndexDividendFuture(timeToFixing, timeToSettlement, PRICE, Currency.CAD, 10.);

    assertEquals(theFuture.getTimeToSettlement(), timeToSettlement, 0);
    assertFalse(Double.compare(theFuture.getTimeToExpiry(), timeToSettlement) == 0);
  }

  @Test
  public void testTimeSeries() {

    final ZonedDateTime lastCloseDate = FIXING_DATE;
    final double lastClose = FIXING_TS.getValue(lastCloseDate);
    assertEquals(lastClose, 98, 0);

    final double latestFixing = FIXING_TS.getLatestValue();
    assertEquals(latestFixing, 100, 0);

    final ZonedDateTime HighNoon = lastCloseDate.plusHours(12);
    assertNull("ArrayZonedDateTimeDoubleTimeSeries.getValue has not returned a null value for a time missing from the series", FIXING_TS.getValue(HighNoon));

    // BLOOMBERG_TICKER~Z H1 Index

  }

}
