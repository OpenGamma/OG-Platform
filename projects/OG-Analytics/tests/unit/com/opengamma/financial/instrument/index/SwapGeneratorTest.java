/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

public class SwapGeneratorTest {
  //Libor3m
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_IBOR = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_IBOR, BUSINESS_DAY, IS_EOM);
  private static final Period FIXED_LEG_PERIOD = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final SwapGenerator GENERATOR = new SwapGenerator(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX);

  @Test
  public void testGetter() {
    assertEquals(FIXED_LEG_PERIOD, GENERATOR.getFixedLegPeriod());
    assertEquals(DAY_COUNT_FIXED, GENERATOR.getFixedLegDayCount());
    assertEquals(IBOR_INDEX, GENERATOR.getIborIndex());
    String name = IBOR_INDEX.getCurrency().toString() + IBOR_INDEX.getTenor().toString() + FIXED_LEG_PERIOD.toString();
    assertEquals(name, GENERATOR.getName());
    assertEquals(GENERATOR.getName(), GENERATOR.toString());
  }

  @Test
  public void testEqualHash() {
    assertEquals(GENERATOR, GENERATOR);
    SwapGenerator generatorDuplicate = new SwapGenerator(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX);
    assertEquals(GENERATOR, generatorDuplicate);
    assertEquals(GENERATOR.hashCode(), generatorDuplicate.hashCode());
    SwapGenerator generatorModified;
    Period otherPeriod = Period.ofMonths(12);
    generatorModified = new SwapGenerator(otherPeriod, DAY_COUNT_FIXED, IBOR_INDEX);
    assertFalse(GENERATOR.equals(generatorModified));
    generatorModified = new SwapGenerator(FIXED_LEG_PERIOD, DAY_COUNT_IBOR, IBOR_INDEX);
    assertFalse(GENERATOR.equals(generatorModified));
    IborIndex otherIborIndex = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_IBOR, BUSINESS_DAY, !IS_EOM);
    generatorModified = new SwapGenerator(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, otherIborIndex);
    assertFalse(GENERATOR.equals(generatorModified));
    assertFalse(GENERATOR.equals(null));
    assertFalse(GENERATOR.equals(CUR));
  }

}
