/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorSwapFixedIborTest {
  //Libor3m
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int SPOT_LAG = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_IBOR = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SPOT_LAG, DAY_COUNT_IBOR, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final Period FIXED_LEG_PERIOD = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCounts.THIRTY_U_360;
  private static final GeneratorSwapFixedIbor GENERATOR_FROM_INDEX = new GeneratorSwapFixedIbor("Swap Generator", FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, CALENDAR);

  private static final BusinessDayConvention BUSINESS_DAY_2 = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_2 = false;
  private static final int SPOT_LAG_2 = 1;
  private static final GeneratorSwapFixedIbor GENERATOR_GENERIC = new GeneratorSwapFixedIbor("Swap Generator", FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, BUSINESS_DAY_2, IS_EOM_2, SPOT_LAG_2, CALENDAR);

  @Test
  public void getter() {
    assertEquals(FIXED_LEG_PERIOD, GENERATOR_FROM_INDEX.getFixedLegPeriod());
    assertEquals(DAY_COUNT_FIXED, GENERATOR_FROM_INDEX.getFixedLegDayCount());
    assertEquals(IBOR_INDEX, GENERATOR_FROM_INDEX.getIborIndex());
    final String name = "Swap Generator";
    assertTrue(name.equals(GENERATOR_FROM_INDEX.getName()));
    assertEquals(GENERATOR_FROM_INDEX.getName(), GENERATOR_FROM_INDEX.toString());
    assertEquals("GeneratorSwap: getter", FIXED_LEG_PERIOD, GENERATOR_GENERIC.getFixedLegPeriod());
    assertEquals("GeneratorSwap: getter", BUSINESS_DAY_2, GENERATOR_GENERIC.getBusinessDayConvention());
    assertTrue("GeneratorSwap: getter", IS_EOM_2 == GENERATOR_GENERIC.isEndOfMonth());
    assertEquals("GeneratorSwap: getter", SPOT_LAG_2, GENERATOR_GENERIC.getSpotLag());
    assertEquals("GeneratorSwap: getter", GENERATOR_FROM_INDEX, new GeneratorSwapFixedIbor("Swap Generator", FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, BUSINESS_DAY, IS_EOM, SPOT_LAG, CALENDAR));
    assertFalse("GeneratorSwap: getter", GENERATOR_FROM_INDEX.equals(GENERATOR_GENERIC));
  }

  @Test
  public void equalHash() {
    assertEquals(GENERATOR_FROM_INDEX, GENERATOR_FROM_INDEX);
    final GeneratorSwapFixedIbor generatorDuplicate = new GeneratorSwapFixedIbor("Swap Generator", FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, CALENDAR);
    assertEquals(GENERATOR_FROM_INDEX, generatorDuplicate);
    assertEquals(GENERATOR_FROM_INDEX.hashCode(), generatorDuplicate.hashCode());
    GeneratorSwapFixedIbor generatorModified;
    final Period otherPeriod = Period.ofMonths(12);
    generatorModified = new GeneratorSwapFixedIbor("Swap Generator", otherPeriod, DAY_COUNT_FIXED, IBOR_INDEX, CALENDAR);
    assertFalse(GENERATOR_FROM_INDEX.equals(generatorModified));
    generatorModified = new GeneratorSwapFixedIbor("Swap Generator", FIXED_LEG_PERIOD, DAY_COUNT_IBOR, IBOR_INDEX, CALENDAR);
    assertFalse(GENERATOR_FROM_INDEX.equals(generatorModified));
    final IborIndex otherIborIndex = new IborIndex(CUR, IBOR_TENOR, SPOT_LAG, DAY_COUNT_IBOR, BUSINESS_DAY, !IS_EOM, "Ibor");
    generatorModified = new GeneratorSwapFixedIbor("Swap Generator", FIXED_LEG_PERIOD, DAY_COUNT_FIXED, otherIborIndex, CALENDAR);
    assertFalse(GENERATOR_FROM_INDEX.equals(generatorModified));
    assertFalse(GENERATOR_FROM_INDEX.equals(null));
    assertFalse(GENERATOR_FROM_INDEX.equals(CUR));
  }

  /**
   * Test fixed payer and fixed receiver 
   */
  @Test
  public void fixedPayerAndReceiverTest() {
    double fixedRate = 0.015;
    GeneratorAttributeIR attribute = new GeneratorAttributeIR(Period.ofYears(7));
    double notional = 1.0e8;
    ZonedDateTime date = DateUtils.getUTCDate(2013, 9, 10);
    SwapFixedIborDefinition def1 = GENERATOR_FROM_INDEX.generateInstrument(date, fixedRate, notional, attribute);
    SwapFixedIborDefinition def2 = GENERATOR_FROM_INDEX.generateInstrument(date, fixedRate, notional, attribute, true);
    assertEquals(def1, def2);
    SwapFixedIborDefinition def3 = GENERATOR_FROM_INDEX.generateInstrument(date, fixedRate, notional, attribute, false);
    assertFalse(def1.equals(def3));
    int nIbor = def1.getIborLeg().getNumberOfPayments();
    int nFixed = def1.getFixedLeg().getNumberOfPayments();
    assertEquals(nIbor, def3.getIborLeg().getNumberOfPayments());
    assertEquals(nFixed, def3.getFixedLeg().getNumberOfPayments());
    assertEquals(def1.getIborLeg().getIborIndex(), def3.getIborLeg().getIborIndex());
    assertEquals(def1.getIborLeg().getCalendar(), def3.getIborLeg().getCalendar());
    assertEquals(def1.getCurrency(), def3.getIborLeg().getCurrency());
    for (int i = 0; i < nIbor; ++i) {
      CouponIborDefinition ibor1 = def1.getIborLeg().getNthPayment(i);
      CouponIborDefinition ibor3 = def3.getIborLeg().getNthPayment(i);
      assertEquals(ibor1.getNotional(), -ibor3.getNotional());
      assertEquals(ibor1.getAccrualStartDate(), ibor3.getAccrualStartDate());
      assertEquals(ibor1.getAccrualEndDate(), ibor3.getAccrualEndDate());
      assertEquals(ibor1.getFixingDate(), ibor3.getFixingDate());
      assertEquals(ibor1.getPaymentDate(), ibor3.getPaymentDate());
      assertEquals(ibor1.getPaymentYearFraction(), ibor3.getPaymentYearFraction());
    }
    for (int i = 0; i < nFixed; ++i) {
      CouponFixedDefinition fixed1 = def1.getFixedLeg().getNthPayment(i);
      CouponFixedDefinition fixed3 = def3.getFixedLeg().getNthPayment(i);
      assertEquals(fixed1.getNotional(), -fixed3.getNotional());
      assertEquals(fixed1.getAccrualStartDate(), fixed3.getAccrualStartDate());
      assertEquals(fixed1.getAccrualEndDate(), fixed3.getAccrualEndDate());
      assertEquals(fixed1.getPaymentDate(), fixed3.getPaymentDate());
      assertEquals(fixed1.getPaymentYearFraction(), fixed3.getPaymentYearFraction());
    }
  }
}
