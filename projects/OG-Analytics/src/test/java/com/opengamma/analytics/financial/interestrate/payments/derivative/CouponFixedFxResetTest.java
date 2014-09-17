/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the constructors and equal/hash for CouponFixedFxReset.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedFxResetTest {
  
  /** Details coupon. */
  private static final Currency CUR_REF = Currency.EUR;
  private static final Currency CUR_PAY = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FX_FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double ACCRUAL_FACTOR = 0.267;
  private static final double NOTIONAL = 1000000; //1m
  private static final double RATE = 0.04;
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(VALUATION_DATE, PAYMENT_DATE);
  private static final double FX_FIXING_TIME = TimeCalculator.getTimeBetween(VALUATION_DATE, FX_FIXING_DATE);
  
  private static final CouponFixedFxReset CPN = new CouponFixedFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR, 
      NOTIONAL, RATE, CUR_REF, FX_FIXING_TIME);
  
  private static final double FX_FIXING_RATE = 1.40;
  
  private static final double TOLERANCE_AMOUNT = 1.0E-6;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullReferenceCurrency() {
    new CouponFixedFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, RATE, null, FX_FIXING_TIME);
  }
  
  @Test
  public void getter() {
    assertEquals("CouponFixedFxReset: getter", RATE, CPN.getRate());
    assertEquals("CouponFixedFxReset: getter", CUR_REF, CPN.getReferenceCurrency());
    assertEquals("CouponFixedFxReset: getter", FX_FIXING_TIME, CPN.getFxFixingTime());
  }
  
  @Test
  public void paymentAmount() {
    double amountExpected = NOTIONAL * FX_FIXING_RATE * RATE * ACCRUAL_FACTOR;
    double amountComputed = CPN.paymentAmount(FX_FIXING_RATE);
    assertEquals("CouponFixedFxResetDefinition: paymentAmount", amountExpected, amountComputed, TOLERANCE_AMOUNT);
  }

  @Test
  public void equalHash() {
    assertEquals("CouponFixedFxReset: hash-equal", CPN, CPN);
    assertFalse("CouponFixedFxReset: hash-equal", CPN.equals(CUR_REF));
    CouponFixedFxReset other = new CouponFixedFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR, 
        NOTIONAL, RATE, CUR_REF, FX_FIXING_TIME);
    assertEquals("CouponFixedFxReset: hash-equal", CPN, other);
    assertEquals("CouponFixedFxResetDefinition: hash-equal", CPN.hashCode(), other.hashCode());
    CouponFixedFxReset modified;
    modified = new CouponFixedFxReset(CUR_REF, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, RATE, CUR_REF, FX_FIXING_TIME);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
    modified = new CouponFixedFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, 0.01, CUR_REF, FX_FIXING_TIME);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
    modified = new CouponFixedFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, RATE, Currency.AUD, FX_FIXING_TIME);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
    modified = new CouponFixedFxReset(CUR_PAY, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, RATE, CUR_REF, 0.0123456);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
  }
  
}
