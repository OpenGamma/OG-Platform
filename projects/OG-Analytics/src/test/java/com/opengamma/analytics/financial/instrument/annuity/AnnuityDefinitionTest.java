/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFloating;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityDefinitionTest {
  private static final Calendar CALENDAR = new NoHolidayCalendar();
  private static final PaymentFixedDefinition[] FIXED_PAYMENTS;
  private static final PaymentDefinition[] FIXED_FLOAT_PAYMENTS;
  private static final Currency CCY = Currency.AUD;
  private static final AnnuityDefinition<PaymentFixedDefinition> FIXED_DEFINITION;
  private static final AnnuityDefinition<PaymentDefinition> FIXED_FLOAT_DEFINITION;
  private static ZonedDateTime FIXING_DATE;
  private static final double FIXING_RATE = 0.02;
  private static ZonedDateTimeDoubleTimeSeries FIXING_TS;
  private static final double ACCRUAL_FACTOR = 1. / 12;
  private static final double FLOAT_NOTIONAL = 1234;

  static {
    final int n = 10;
    FIXED_PAYMENTS = new PaymentFixedDefinition[n];
    FIXED_FLOAT_PAYMENTS = new PaymentDefinition[n];
    ZonedDateTime date = DateUtils.getUTCDate(2011, 1, 1);
    final IborIndex index = new IborIndex(CCY, Period.ofMonths(3), 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "Ibor");
    for (int i = 0; i < n; i++) {
      FIXED_PAYMENTS[i] = new PaymentFixedDefinition(CCY, date, 1000);
      FIXED_FLOAT_PAYMENTS[i] = (i < 8 ? new CouponFixedDefinition(CCY, date, date.minusMonths(1), date, ACCRUAL_FACTOR, 1000, 0.05) : CouponIborDefinition.from(date, date.minusMonths(1), date,
          ACCRUAL_FACTOR, FLOAT_NOTIONAL, date.minusMonths(1), index, new MondayToFridayCalendar("A")));
      if (i == 8) {
        FIXING_DATE = date.minusMonths(1);
      }
      date = date.plusMonths(1);
    }
    FIXED_DEFINITION = new AnnuityDefinition<>(FIXED_PAYMENTS, CALENDAR);
    FIXED_FLOAT_DEFINITION = new AnnuityDefinition<>(FIXED_FLOAT_PAYMENTS, CALENDAR);
    FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.of(FIXING_DATE, FIXING_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayments() {
    new AnnuityDefinition<>((PaymentFixedDefinition[]) null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithNullPayment() {
    final PaymentFixedDefinition[] payments = Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length);
    payments[0] = null;
    new AnnuityDefinition<>(payments, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPayments() {
    new AnnuityDefinition<>(new PaymentFixedDefinition[0], null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentCurrencyPayments() {
    final PaymentFixedDefinition[] payments = Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length);
    payments[0] = new PaymentFixedDefinition(Currency.CAD, DateUtils.getUTCDate(2011, 1, 1), 1000);
    new AnnuityDefinition<>(payments, null);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullDate1Deprecated() {
    FIXED_DEFINITION.toDerivative(null, new String[] {"E", "F" });
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullDate2Deprecated() {
    FIXED_DEFINITION.toDerivative(null, FIXING_TS, new String[] {"E", "F" });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullDate1() {
    FIXED_DEFINITION.toDerivative(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullDate2() {
    FIXED_DEFINITION.toDerivative(null, FIXING_TS);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullNames1Deprecated() {
    FIXED_DEFINITION.toDerivative(FIXING_DATE, (String[]) null);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullNames2() {
    FIXED_DEFINITION.toDerivative(FIXING_DATE, FIXING_TS, (String[]) null);
  }

  @Test
  public void testObject() {
    assertEquals(FIXED_PAYMENTS, FIXED_DEFINITION.getPayments());
    for (int i = 0; i < FIXED_PAYMENTS.length; i++) {
      assertEquals(FIXED_PAYMENTS[i], FIXED_DEFINITION.getNthPayment(i));
    }
    assertEquals(CCY, FIXED_DEFINITION.getCurrency());
    assertEquals(FIXED_PAYMENTS.length, FIXED_DEFINITION.getNumberOfPayments());
    AnnuityDefinition<PaymentFixedDefinition> other = new AnnuityDefinition<>(FIXED_PAYMENTS, CALENDAR);
    assertEquals(FIXED_DEFINITION, other);
    assertEquals(FIXED_DEFINITION.hashCode(), other.hashCode());
    final PaymentFixedDefinition[] payments = Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length);
    payments[0] = new PaymentFixedDefinition(CCY, DateUtils.getUTCDate(2011, 1, 1), 10000);
    other = new AnnuityDefinition<>(payments, CALENDAR);
    assertFalse(other.equals(FIXED_DEFINITION));
  }

  @Test
  public void testPayer() {
    assertFalse(FIXED_DEFINITION.isPayer());
    PaymentFixedDefinition[] payments = new PaymentFixedDefinition[FIXED_PAYMENTS.length];
    for (int i = 0; i < FIXED_PAYMENTS.length; i++) {
      payments[i] = new PaymentFixedDefinition(CCY, DateUtils.getUTCDate(2011, 1, 1), -1000);
    }
    assertTrue(new AnnuityDefinition<>(payments, CALENDAR).isPayer());
    payments = Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length);
    payments[0] = new PaymentFixedDefinition(CCY, DateUtils.getUTCDate(2011, 1, 1), 0);
    assertFalse(FIXED_DEFINITION.isPayer());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testConversionFixedDeprecated() {
    final ZonedDateTime date = DateUtils.getUTCDate(2011, 5, 10);
    final Annuity<? extends Payment> annuity1 = FIXED_DEFINITION.toDerivative(date, "A");
    final Annuity<? extends Payment> annuity2 = FIXED_DEFINITION.toDerivative(date, FIXING_TS, "A");
    assertEquals(FIXED_DEFINITION.getNumberOfPayments(), 10);
    assertEquals(annuity1.getNumberOfPayments(), 5);
    for (int i = 0; i < annuity1.getNumberOfPayments(); i++) {
      assertTrue(annuity1.getNthPayment(i) instanceof PaymentFixed);
      assertEquals(annuity1.getNthPayment(i), FIXED_DEFINITION.getNthPayment(i + 5).toDerivative(date, "A"));
      assertEquals(annuity2.getNthPayment(i), FIXED_DEFINITION.getNthPayment(i + 5).toDerivative(date, "A"));
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testConversionFixedFloatDeprecated() {
    final ZonedDateTime date = DateUtils.getUTCDate(2011, 5, 10);
    final Annuity<? extends Payment> annuity = FIXED_FLOAT_DEFINITION.toDerivative(date, FIXING_TS, "A", "N");
    assertEquals(FIXED_DEFINITION.getNumberOfPayments(), 10);
    assertEquals(annuity.getNumberOfPayments(), 5);
    for (int i = 0; i < annuity.getNumberOfPayments(); i++) {
      if (i < 3) {
        assertTrue(annuity.getNthPayment(i) instanceof CouponFixed);
        assertEquals(annuity.getNthPayment(i), FIXED_FLOAT_DEFINITION.getNthPayment(i + 5).toDerivative(date, "A", "N"));
      } else {
        assertTrue(annuity.getNthPayment(i) instanceof CouponFloating);
        assertEquals(annuity.getNthPayment(i), FIXED_FLOAT_DEFINITION.getNthPayment(i + 5).toDerivative(date, "A", "N"));
      }
    }
  }

  @Test
  public void testConversionFixed() {
    final ZonedDateTime date = DateUtils.getUTCDate(2011, 5, 10);
    final Annuity<? extends Payment> annuity1 = FIXED_DEFINITION.toDerivative(date);
    final Annuity<? extends Payment> annuity2 = FIXED_DEFINITION.toDerivative(date, FIXING_TS);
    assertEquals(FIXED_DEFINITION.getNumberOfPayments(), 10);
    assertEquals(annuity1.getNumberOfPayments(), 5);
    for (int i = 0; i < annuity1.getNumberOfPayments(); i++) {
      assertTrue(annuity1.getNthPayment(i) instanceof PaymentFixed);
      assertEquals(annuity1.getNthPayment(i), FIXED_DEFINITION.getNthPayment(i + 5).toDerivative(date));
      assertEquals(annuity2.getNthPayment(i), FIXED_DEFINITION.getNthPayment(i + 5).toDerivative(date));
    }
  }

  @Test
  public void testConversionFixedFloat() {
    final ZonedDateTime date = DateUtils.getUTCDate(2011, 5, 10);
    final Annuity<? extends Payment> annuity = FIXED_FLOAT_DEFINITION.toDerivative(date, FIXING_TS);
    assertEquals(FIXED_DEFINITION.getNumberOfPayments(), 10);
    assertEquals(annuity.getNumberOfPayments(), 5);
    for (int i = 0; i < annuity.getNumberOfPayments(); i++) {
      if (i < 3) {
        assertTrue(annuity.getNthPayment(i) instanceof CouponFixed);
        assertEquals(annuity.getNthPayment(i), FIXED_FLOAT_DEFINITION.getNthPayment(i + 5).toDerivative(date));
      } else {
        assertTrue(annuity.getNthPayment(i) instanceof CouponFloating);
        assertEquals(annuity.getNthPayment(i), FIXED_FLOAT_DEFINITION.getNthPayment(i + 5).toDerivative(date));
      }
    }
  }
}
