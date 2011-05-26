/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future;


import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.equity.future.derivative.EquityIndexDividendFuture;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

/**
 * 
 */
public class EquityIndexDividendFutureTest {

  public static final double PRICE = 95.0;
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime[] FIXING_DATES =  
    {FIXING_DATE, 
    DateUtil.getDateOffsetWithYearFraction(FIXING_DATE,1.0),
    FIXING_DATE.plusYears(1)}; 
  private static final double[] FIXINGS = {98d,99.,100.0};
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(FIXING_DATES, FIXINGS);
  
  @Test
  public void test() {
    final double settlement = 1.45;
    final double fixing = 1.44;
    final EquityIndexDividendFuture theFuture = new EquityIndexDividendFuture(fixing, settlement, PRICE, 10., "DIVIDX");
    
    assertEquals(theFuture.getTimeToDelivery(),settlement,0);
    assertFalse(theFuture.getTimeToFixing()==settlement);
  }

  @Test
  public void testTimeSeries() {

    // BLOOMBERG_TICKER~Z H1 Index
    
  }
  
}
