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

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedOISDefinitionTest {

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);

  // EONIA tests
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedON EONIA_GENERATOR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", EUR_CALENDAR);

  private static final double NOTIONAL = 100000000;
  private static final double FIXED_RATE = 0.01;
  private static final boolean IS_PAYER = true;
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EONIA_GENERATOR.getSpotLag(), EUR_CALENDAR);
  // Swap EONIA 3M
  private static final Period EUR_SWAP_3M_TENOR = Period.ofMonths(3);
  private static final SwapFixedONDefinition EONIA_SWAP_3M_DEFINITION = SwapFixedONDefinition.from(SPOT_DATE, EUR_SWAP_3M_TENOR, NOTIONAL, EONIA_GENERATOR, FIXED_RATE, IS_PAYER);
  // Swap EONIA 3Y
  private static final Period EUR_SWAP_3Y_TENOR = Period.ofYears(3);
  private static final SwapFixedONDefinition EONIA_SWAP_3Y_DEFINITION = SwapFixedONDefinition.from(SPOT_DATE, EUR_SWAP_3Y_TENOR, NOTIONAL, EONIA_GENERATOR, FIXED_RATE, IS_PAYER);

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
    paymentDate = ScheduleCalculator.getAdjustedDate(paymentDate, EONIA_GENERATOR.getIndex().getPublicationLag(), EUR_CALENDAR);
    paymentDate = ScheduleCalculator.getAdjustedDate(paymentDate, EONIA_GENERATOR.getPaymentLag(), EUR_CALENDAR);
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
          EONIA_GENERATOR
              .getIndex()
              .getDayCount()
              .getDayCountFraction(EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualStartDate(),
                  EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualEndDate()), EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getPaymentYearFraction(), 1.0E-10);
      assertFalse("Swap OIS definition: constructor",
          EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getPaymentDate().equals(EONIA_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualEndDate()));
      // In EUR the payment date and the end accrual date are one day apart.
    }
    final ZonedDateTime eurMaturity3Y = SPOT_DATE.plus(EUR_SWAP_3Y_TENOR);
    SwapFixedONDefinition eoniaSwap3YDefinitionFromMaturity = SwapFixedONDefinition.from(SPOT_DATE, eurMaturity3Y, NOTIONAL, EONIA_GENERATOR, FIXED_RATE, IS_PAYER);
    assertEquals("Swap OIS definition: constructor", EONIA_SWAP_3Y_DEFINITION, eoniaSwap3YDefinitionFromMaturity);
  }

  // EONIA tests
  private static final Calendar AUD_CALENDAR = new MondayToFridayCalendar("SYDNEY");
  private static final GeneratorSwapFixedON RBAON_GENERATOR = GeneratorSwapFixedONMaster.getInstance().getGenerator("AUD1YRBAON", AUD_CALENDAR);

  private static final double AUD_NOTIONAL = 100000000;
  private static final double AUD_FIXED_RATE = 0.01;
  private static final boolean AUD_IS_PAYER = true;
  private static final ZonedDateTime AUD_SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, RBAON_GENERATOR.getSpotLag(), AUD_CALENDAR);
  // Swap EONIA 3M
  private static final Period AUD_SWAP_3M_TENOR = Period.ofMonths(3);
  private static final SwapFixedONDefinition RBAON_SWAP_3M_DEFINITION = SwapFixedONDefinition.from(AUD_SPOT_DATE, AUD_SWAP_3M_TENOR, AUD_NOTIONAL, RBAON_GENERATOR, AUD_FIXED_RATE, AUD_IS_PAYER);
  // Swap EONIA 3Y
  private static final Period AUD_SWAP_3Y_TENOR = Period.ofYears(3);
  private static final SwapFixedONDefinition RBAON_SWAP_3Y_DEFINITION = SwapFixedONDefinition.from(AUD_SPOT_DATE, AUD_SWAP_3Y_TENOR, AUD_NOTIONAL, RBAON_GENERATOR, AUD_FIXED_RATE, AUD_IS_PAYER);

  @Test
  public void constructionAUD3M() {
    assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3M_DEFINITION.getFirstLeg().getNumberOfPayments(), 1);
    assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3M_DEFINITION.getSecondLeg().getNumberOfPayments(), 1);
    assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3M_DEFINITION.getFirstLeg().getNthPayment(0).getPaymentDate(), RBAON_SWAP_3M_DEFINITION.getSecondLeg().getNthPayment(0)
        .getPaymentDate());
    assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3M_DEFINITION.getFixedLeg().getNthPayment(0).getAccrualStartDate(), RBAON_SWAP_3M_DEFINITION.getOISLeg().getNthPayment(0)
        .getAccrualStartDate());
    assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3M_DEFINITION.getFixedLeg().getNthPayment(0).getAccrualEndDate(), RBAON_SWAP_3M_DEFINITION.getOISLeg().getNthPayment(0)
        .getAccrualEndDate());
    ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(RBAON_SWAP_3M_DEFINITION.getFixedLeg().getNthPayment(0).getAccrualEndDate(), -1, AUD_CALENDAR); // Overnight
    paymentDate = ScheduleCalculator.getAdjustedDate(paymentDate, RBAON_GENERATOR.getIndex().getPublicationLag(), AUD_CALENDAR);
    paymentDate = ScheduleCalculator.getAdjustedDate(paymentDate, RBAON_GENERATOR.getPaymentLag(), AUD_CALENDAR);
    assertEquals("Swap OIS definition: constructor", paymentDate, RBAON_SWAP_3M_DEFINITION.getFirstLeg().getNthPayment(0).getPaymentDate());
  }

  @Test
  public void constructionAUD3Y() {
    assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3Y_DEFINITION.getFirstLeg().getNumberOfPayments(), 3);
    assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3Y_DEFINITION.getSecondLeg().getNumberOfPayments(), 3);
    for (int loopcpn = 0; loopcpn < RBAON_SWAP_3Y_DEFINITION.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3Y_DEFINITION.getFirstLeg().getNthPayment(loopcpn).getPaymentDate(), RBAON_SWAP_3Y_DEFINITION.getSecondLeg().getNthPayment(loopcpn)
          .getPaymentDate());
      assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualStartDate(), RBAON_SWAP_3Y_DEFINITION.getOISLeg().getNthPayment(loopcpn)
          .getAccrualStartDate());
      assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualEndDate(), RBAON_SWAP_3Y_DEFINITION.getOISLeg().getNthPayment(loopcpn)
          .getAccrualEndDate());
      assertEquals(
          "Swap OIS definition: constructor",
          RBAON_GENERATOR
              .getIndex()
              .getDayCount()
              .getDayCountFraction(RBAON_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualStartDate(),
                  RBAON_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getAccrualEndDate()), RBAON_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getPaymentYearFraction(), 1.0E-10);
      assertEquals("Swap OIS definition: constructor", RBAON_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn).getPaymentDate(), RBAON_SWAP_3Y_DEFINITION.getFixedLeg().getNthPayment(loopcpn)
          .getAccrualEndDate());
      // In AUD the payment date and the end accrual date are equal.
    }
  }

}
