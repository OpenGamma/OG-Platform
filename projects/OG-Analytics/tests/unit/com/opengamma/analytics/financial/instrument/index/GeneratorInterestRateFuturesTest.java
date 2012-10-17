/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the generator of interest rate futures.
 */
public class GeneratorInterestRateFuturesTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR3M = IBOR_MASTER.getIndex("USDLIBOR3M", NYC);
  private static final GeneratorInterestRateFutures GENERATOR_FUTURES_ED = new GeneratorInterestRateFutures("USD-ED", USDLIBOR3M, 0.25);

  @Test
  /**
   * Tests the getter for the futures generator.
   */
  public void getter() {
    assertEquals("GeneratorInterestRateFutures: getter", USDLIBOR3M, GENERATOR_FUTURES_ED.getIborIndex());
    assertEquals("GeneratorInterestRateFutures: getter", GENERATOR_FUTURES_ED.getName(), "USD-ED");
  }

  @Test
  public void generateInstrument() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    Period tenorInit = Period.ofMonths(1);
    Integer num = 2;
    double price = 0.99;
    double notional = 100000;
    InterestRateFutureDefinition insGenerated = GENERATOR_FUTURES_ED.generateInstrument(referenceDate, tenorInit, price, notional, num);
    ZonedDateTime fixingPeriodStartDate = DateUtils.getUTCDate(2012, 12, 19);
    ZonedDateTime lastTradingDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, -USDLIBOR3M.getSpotLag(), NYC);
    ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, USDLIBOR3M);
    InterestRateFutureDefinition insExpected = new InterestRateFutureDefinition(referenceDate, price, 1, lastTradingDate, fixingPeriodStartDate, fixingPeriodEndDate, USDLIBOR3M, notional, 0.25, "IRF");
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated);
  }

}
