/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests {@link CashflowEquivalentTheoreticalCalculator}.
 */
public class CashflowEquivalentTheoreticalCalculatorTest {

  private static final ZonedDateTime VAlUATION_DATE_1 = DateUtils.getUTCDate(2015, 6, 5);
  private static final double NOTIONAL = 10_000_000;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2015, 8, 18);
  private static final double START_TIME_1 = TimeCalculator.getTimeBetween(VAlUATION_DATE_1, START_DATE);
  private static final ZonedDateTime END_DATE = DateUtils.getUTCDate(2015, 11, 18);
  private static final double END_TIME_1 = TimeCalculator.getTimeBetween(VAlUATION_DATE_1, END_DATE);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2015, 12, 18);
  private static final double PAYMENT_TIME_1 = TimeCalculator.getTimeBetween(VAlUATION_DATE_1, PAYMENT_DATE);
  private static final double AF = 0.54;
  /* Fixed coupon */
  private static final double FIXED_RATE = 0.01;
  private static final CouponFixed CPN_FIXED = new CouponFixed(Currency.EUR, PAYMENT_TIME_1, AF, NOTIONAL, FIXED_RATE);
  /* Ibor coupon */
  private static final ZonedDateTime IBOR_FIXING_DATE = DateUtils.getUTCDate(2015, 8, 10);
  private static final double IBOR_FIXING_TIME_1 = TimeCalculator.getTimeBetween(VAlUATION_DATE_1, IBOR_FIXING_DATE);
  private static final IborIndex EURIBOR6M = IndexIborMaster.getInstance().getIndex(IndexIborMaster.EURIBOR6M);
  private static final CouponIbor CPN_IBOR = new CouponIbor(Currency.EUR, PAYMENT_TIME_1, AF, NOTIONAL, 
      IBOR_FIXING_TIME_1, EURIBOR6M, START_TIME_1, END_TIME_1, AF);
  /* Pricer */
  private static final CashflowEquivalentTheoreticalCalculator CFEC = 
      CashflowEquivalentTheoreticalCalculator.getInstance();
  /* Tolerance */
  private static final double TOLERANCE_CF = 1.0E-2;
  
  @Test
  public void test_CouponFixed() {
    AnnuityPaymentFixed cfeComputed = CPN_FIXED.accept(CFEC);
    assertEquals("CFE - Fixed", cfeComputed.getNumberOfPayments(), 1);
    PaymentFixed cfePayment = cfeComputed.getNthPayment(0);
    assertEquals(cfePayment.getPaymentTime(), PAYMENT_TIME_1);
    assertEquals(cfePayment.getCurrency(), Currency.EUR);
    assertEquals(cfePayment.getAmount(), NOTIONAL * FIXED_RATE * AF, TOLERANCE_CF);
  }
  
  @Test
  public void test_CouponIbor() {
    AnnuityPaymentFixed cfeComputed = CPN_IBOR.accept(CFEC);
    assertEquals("CFE - Fixed", cfeComputed.getNumberOfPayments(), 2);
    PaymentFixed cfePayment1 = cfeComputed.getNthPayment(0);
    assertEquals(cfePayment1.getPaymentTime(), START_TIME_1);
    assertEquals(cfePayment1.getCurrency(), Currency.EUR);
    assertEquals(cfePayment1.getAmount(), NOTIONAL, TOLERANCE_CF);
    PaymentFixed cfePayment2 = cfeComputed.getNthPayment(1);
    assertEquals(cfePayment2.getPaymentTime(), END_TIME_1);
    assertEquals(cfePayment2.getCurrency(), Currency.EUR);
    assertEquals(cfePayment2.getAmount(), - NOTIONAL, TOLERANCE_CF);    
  }
  
  @Test
  public void test_CouponIborSpread() {
    
  }
  
  @Test
  public void test_CouponIborGearing() {
    
  }
  
}
