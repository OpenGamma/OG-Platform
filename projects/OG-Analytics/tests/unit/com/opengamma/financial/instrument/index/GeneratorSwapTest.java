/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

public class GeneratorSwapTest {
  //Libor3m
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int SPOT_LAG = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_IBOR = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SPOT_LAG, CALENDAR, DAY_COUNT_IBOR, BUSINESS_DAY, IS_EOM);
  private static final Period FIXED_LEG_PERIOD = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final GeneratorSwap GENERATOR_FROM_INDEX = new GeneratorSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX);

  private static final BusinessDayConvention BUSINESS_DAY_2 = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_2 = false;
  private static final int SPOT_LAG_2 = 1;
  private static final GeneratorSwap GENERATOR_GENERIC = new GeneratorSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, BUSINESS_DAY_2, IS_EOM_2, SPOT_LAG_2);

  @Test
  public void getter() {
    assertEquals(FIXED_LEG_PERIOD, GENERATOR_FROM_INDEX.getFixedLegPeriod());
    assertEquals(DAY_COUNT_FIXED, GENERATOR_FROM_INDEX.getFixedLegDayCount());
    assertEquals(IBOR_INDEX, GENERATOR_FROM_INDEX.getIborIndex());
    String name = IBOR_INDEX.getCurrency().toString() + IBOR_INDEX.getTenor().toString() + FIXED_LEG_PERIOD.toString();
    assertEquals(name, GENERATOR_FROM_INDEX.getName());
    assertEquals(GENERATOR_FROM_INDEX.getName(), GENERATOR_FROM_INDEX.toString());
    assertEquals("GeneratorSwap: getter", FIXED_LEG_PERIOD, GENERATOR_GENERIC.getFixedLegPeriod());
    assertEquals("GeneratorSwap: getter", BUSINESS_DAY_2, GENERATOR_GENERIC.getBusinessDayConvention());
    assertTrue("GeneratorSwap: getter", IS_EOM_2 == GENERATOR_GENERIC.isEndOfMonth());
    assertEquals("GeneratorSwap: getter", SPOT_LAG_2, GENERATOR_GENERIC.getSpotLag());
    assertEquals("GeneratorSwap: getter", GENERATOR_FROM_INDEX, new GeneratorSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, BUSINESS_DAY, IS_EOM, SPOT_LAG));
    assertFalse("GeneratorSwap: getter", GENERATOR_FROM_INDEX.equals(GENERATOR_GENERIC));
  }

  @Test
  public void equalHash() {
    assertEquals(GENERATOR_FROM_INDEX, GENERATOR_FROM_INDEX);
    GeneratorSwap generatorDuplicate = new GeneratorSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX);
    assertEquals(GENERATOR_FROM_INDEX, generatorDuplicate);
    assertEquals(GENERATOR_FROM_INDEX.hashCode(), generatorDuplicate.hashCode());
    GeneratorSwap generatorModified;
    Period otherPeriod = Period.ofMonths(12);
    generatorModified = new GeneratorSwap(otherPeriod, DAY_COUNT_FIXED, IBOR_INDEX);
    assertFalse(GENERATOR_FROM_INDEX.equals(generatorModified));
    generatorModified = new GeneratorSwap(FIXED_LEG_PERIOD, DAY_COUNT_IBOR, IBOR_INDEX);
    assertFalse(GENERATOR_FROM_INDEX.equals(generatorModified));
    IborIndex otherIborIndex = new IborIndex(CUR, IBOR_TENOR, SPOT_LAG, CALENDAR, DAY_COUNT_IBOR, BUSINESS_DAY, !IS_EOM);
    generatorModified = new GeneratorSwap(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, otherIborIndex);
    assertFalse(GENERATOR_FROM_INDEX.equals(generatorModified));
    assertFalse(GENERATOR_FROM_INDEX.equals(null));
    assertFalse(GENERATOR_FROM_INDEX.equals(CUR));
  }

}
