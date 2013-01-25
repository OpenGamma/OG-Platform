/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

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
  private static final ZonedDateTime FIXING_PERIOD_START_DATE = DateUtils.getUTCDate(2012, 12, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(FIXING_PERIOD_START_DATE, -USDLIBOR3M.getSpotLag(), NYC);
  private static final ZonedDateTime FIXING_PERIOD_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_PERIOD_START_DATE, USDLIBOR3M);
  private static final double NOTIONAL = 100000;
  private static final InterestRateFutureDefinition FUTURES_DEFINITION = new InterestRateFutureDefinition(LAST_TRADING_DATE, 0, 1, LAST_TRADING_DATE, FIXING_PERIOD_START_DATE, FIXING_PERIOD_END_DATE,
      USDLIBOR3M, NOTIONAL, 0.25, "IRF");
  private static final GeneratorInterestRateFutures GENERATOR_FUTURES_ED = new GeneratorInterestRateFutures("USD-ED", FUTURES_DEFINITION);

  @Test
  /**
   * Tests the getter for the futures generator.
   */
  public void getter() {
    assertEquals("GeneratorInterestRateFutures: getter", FUTURES_DEFINITION, GENERATOR_FUTURES_ED.getFutures());
    assertEquals("GeneratorInterestRateFutures: getter", GENERATOR_FUTURES_ED.getName(), "USD-ED");
  }

  @Test
  public void generateInstrument() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    //    Period tenorInit = Period.ofMonths(1);
    //    Integer num = 2;
    double price = 0.99;
    double notional = 100000;
    GeneratorAttribute attribute = new GeneratorAttribute();
    InterestRateFutureDefinition insGenerated = GENERATOR_FUTURES_ED.generateInstrument(referenceDate, price, notional, attribute);
    InterestRateFutureDefinition insExpected = new InterestRateFutureDefinition(referenceDate, price, 1, LAST_TRADING_DATE, FIXING_PERIOD_START_DATE, FIXING_PERIOD_END_DATE, USDLIBOR3M, notional,
        0.25, "IRF");
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated);
  }

}
