/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.threeten.bp.temporal.ChronoUnit.DAYS;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the constructor and method of GeneratorDepositON.
 */
public class GeneratorDepositONCounterpartTest {

  private static final String NAME_GENERATOR = "US Govt Deposit ON";
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final Currency CUR = Currency.EUR;
  private static final String NAME_COUNTERPART = "US GOVT";
  private static final GeneratorDepositONCounterpart GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositONCounterpart(NAME_GENERATOR, CUR, CALENDAR, DAY_COUNT, NAME_COUNTERPART);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullGenName() {
    new GeneratorDepositONCounterpart(null, CUR, CALENDAR, DAY_COUNT, NAME_COUNTERPART);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorDepositONCounterpart(NAME_GENERATOR, null, CALENDAR, DAY_COUNT, NAME_COUNTERPART);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar() {
    new GeneratorDepositONCounterpart(NAME_GENERATOR, CUR, null, DAY_COUNT, NAME_COUNTERPART);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new GeneratorDepositONCounterpart(NAME_GENERATOR, CUR, CALENDAR, null, NAME_COUNTERPART);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCounterpartName() {
    new GeneratorDepositONCounterpart(NAME_GENERATOR, CUR, CALENDAR, DAY_COUNT, null);
  }

  @Test
  public void getter() {
    assertEquals("Generator Deposit Counterpart: getter", NAME_GENERATOR, GENERATOR_DEPOSIT_ON_USD.getName());
    assertEquals("Generator Deposit Counterpart: getter", CUR, GENERATOR_DEPOSIT_ON_USD.getCurrency());
    assertEquals("Generator Deposit Counterpart: getter", CALENDAR, GENERATOR_DEPOSIT_ON_USD.getCalendar());
    assertEquals("Generator Deposit Counterpart: getter", DAY_COUNT, GENERATOR_DEPOSIT_ON_USD.getDayCount());
    assertEquals("Generator Deposit Counterpart: getter", NAME_COUNTERPART, GENERATOR_DEPOSIT_ON_USD.getNameCounterpart());
  }

  @Test
  public void generateInstrument() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    Period tenor = Period.ofDays(2);
    double rate = 0.01;
    double notional = 12345;
    GeneratorAttributeIR attribute = new GeneratorAttributeIR(tenor, tenor);
    DepositCounterpartDefinition insGenerated = GENERATOR_DEPOSIT_ON_USD.generateInstrument(referenceDate, rate, notional, attribute);
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(referenceDate, tenor, CALENDAR);
    ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, CALENDAR);
    double accrualFactor = DAY_COUNT.getDayCountFraction(startDate, endDate);
    DepositCounterpartDefinition insExpected = new DepositCounterpartDefinition(CUR, startDate, endDate, notional, rate, accrualFactor, NAME_COUNTERPART);
    assertEquals("Generator Deposit Counterpart: generate instrument", insExpected, insGenerated);
  }

}
