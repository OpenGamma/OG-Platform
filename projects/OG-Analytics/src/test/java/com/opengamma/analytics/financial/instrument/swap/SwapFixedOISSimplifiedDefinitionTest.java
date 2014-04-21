/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
 * Tests related to the construction of OIS swaps.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedOISSimplifiedDefinitionTest {

  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCounts.ACT_360;
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG);

  private static final double NOTIONAL = 100000000;
  private static final double FIXED_RATE = 0.01;
  private static final boolean IS_PAYER = true;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);

  // Swap EONIA 3M
  private static final Period EUR_SWAP_3M_TENOR = Period.ofMonths(3);
  private static final SwapFixedONSimplifiedDefinition EONIA_SWAP_3M_DEFINITION = SwapFixedONSimplifiedDefinition.from(SPOT_DATE, EUR_SWAP_3M_TENOR, EUR_SWAP_3M_TENOR, NOTIONAL, EUR_OIS,
      FIXED_RATE, IS_PAYER, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_DAY_COUNT, EUR_IS_EOM, EUR_CALENDAR);

  // Swap EONIA 3Y
  private static final Period EUR_SWAP_3Y_TENOR = Period.ofYears(3);
  private static final Period EUR_COUPON_TENOR = Period.ofMonths(12);
  private static final SwapFixedONSimplifiedDefinition EONIA_SWAP_3Y_DEFINITION = SwapFixedONSimplifiedDefinition.from(SPOT_DATE, EUR_SWAP_3Y_TENOR, EUR_COUPON_TENOR, NOTIONAL, EUR_OIS, FIXED_RATE,
      IS_PAYER, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_DAY_COUNT, EUR_IS_EOM, EUR_CALENDAR);

  @Test
  public void construction3M() {
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3M_DEFINITION.getFirstLeg().getNumberOfPayments(), 1);
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3M_DEFINITION.getSecondLeg().getNumberOfPayments(), 1);
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3M_DEFINITION.getFirstLeg().getNthPayment(0).getPaymentDate(), EONIA_SWAP_3M_DEFINITION.getSecondLeg().getNthPayment(0)
        .getPaymentDate());
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3M_DEFINITION.getFixedLeg().getNthPayment(0).getAccrualStartDate(), EONIA_SWAP_3M_DEFINITION.getOISLeg().getNthPayment(0)
        .getAccrualStartDate());
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3M_DEFINITION.getFixedLeg().getNthPayment(0).getAccrualEndDate(), EONIA_SWAP_3M_DEFINITION.getOISLeg().getNthPayment(0)
        .getAccrualEndDate());
    ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(EONIA_SWAP_3M_DEFINITION.getFixedLeg().getNthPayment(0).getAccrualEndDate(), -1, EUR_CALENDAR); // Overnight
    paymentDate = ScheduleCalculator.getAdjustedDate(paymentDate, EUR_PUBLICATION_LAG, EUR_CALENDAR); // Publication lag
    paymentDate = ScheduleCalculator.getAdjustedDate(paymentDate, EUR_SETTLEMENT_DAYS, EUR_CALENDAR); // Payment lag
    assertEquals("Swap OIS definition: constructor", paymentDate, EONIA_SWAP_3M_DEFINITION.getFirstLeg().getNthPayment(0).getPaymentDate());
  }

  @Test
  public void construction3Y() {
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3Y_DEFINITION.getFirstLeg().getNumberOfPayments(), 3);
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3Y_DEFINITION.getSecondLeg().getNumberOfPayments(), 3);
    for (int loopcpn = 0; loopcpn < EONIA_SWAP_3Y_DEFINITION.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3Y_DEFINITION.getFirstLeg().getNthPayment(loopcpn).getPaymentDate(), EONIA_SWAP_3Y_DEFINITION.getSecondLeg().getNthPayment(loopcpn)
          .getPaymentDate());
      assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualStartDate(), EONIA_SWAP_3Y_DEFINITION.getOISLeg().getNthPayment(loopcpn)
          .getAccrualStartDate());
      assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualEndDate(), EONIA_SWAP_3Y_DEFINITION.getOISLeg().getNthPayment(loopcpn)
          .getAccrualEndDate());
      assertEquals(
          "Swap OIS definition: constructor",
          EUR_DAY_COUNT.getDayCountFraction(EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualStartDate(), EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn)
              .getAccrualEndDate()), EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getPaymentYearFraction(), 1.0E-10);
      assertFalse("Swap OIS definition: constructor",
          EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getPaymentDate().equals(EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualEndDate()));
    }
  }

}
