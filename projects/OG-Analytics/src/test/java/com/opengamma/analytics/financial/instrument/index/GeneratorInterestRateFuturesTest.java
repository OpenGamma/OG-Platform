/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the generator of interest rate futures.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorInterestRateFuturesTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR3M = IBOR_MASTER.getIndex("USDLIBOR3M");
  private static final ZonedDateTime FIXING_PERIOD_START_DATE = DateUtils.getUTCDate(2012, 12, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(FIXING_PERIOD_START_DATE, -USDLIBOR3M.getSpotLag(), NYC);
  private static final double NOTIONAL = 1000000;
  private static final InterestRateFutureSecurityDefinition FUTURES_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, USDLIBOR3M, NOTIONAL, 0.25, "IRF", NYC);
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
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    final double price = 0.99;
    final double notional = 2000000;
    final int quantity = (int) Math.ceil(notional / NOTIONAL);
    final GeneratorAttribute attribute = new GeneratorAttribute();
    final InterestRateFutureTransactionDefinition insGenerated = GENERATOR_FUTURES_ED.generateInstrument(referenceDate, price, notional, attribute);
    final InterestRateFutureTransactionDefinition insExpected = new InterestRateFutureTransactionDefinition(FUTURES_DEFINITION, quantity, referenceDate, price);
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated);
  }

}
