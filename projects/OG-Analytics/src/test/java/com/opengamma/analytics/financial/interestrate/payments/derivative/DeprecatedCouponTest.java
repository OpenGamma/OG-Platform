/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCouponTest {
  private static final Currency CCY = Currency.AUD;
  private static final double PAYMENT_TIME = 0.5;
  private static final String NAME = "D";
  private static final double ACCRUAL_TIME = 0.51;
  private static final double NOTIONAL = 100;
  private static final Coupon COUPON = new MyCoupon(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeAccrualTime() {
    new MyCoupon(CCY, PAYMENT_TIME, NAME, -ACCRUAL_TIME, NOTIONAL);
  }

  @Test
  public void testObject() {
    assertEquals(CCY, COUPON.getCurrency());
    assertEquals(NAME, COUPON.getFundingCurveName());
    assertEquals(NOTIONAL, COUPON.getNotional(), 0);
    assertEquals(PAYMENT_TIME, COUPON.getPaymentTime(), 0);
    assertEquals(ACCRUAL_TIME, COUPON.getPaymentYearFraction(), 0);
    assertEquals(NOTIONAL, COUPON.getReferenceAmount(), 0);
    Coupon other = new MyCoupon(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL);
    assertEquals(COUPON, other);
    assertEquals(COUPON.hashCode(), other.hashCode());
    assertEquals(COUPON.toString(), "Currency=AUD, payment time=0.5, funding curve=D, year fraction = 0.51, notional = 100.0");
    other = new MyCoupon(Currency.CAD, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL);
    assertFalse(other.equals(COUPON));
    other = new MyCoupon(CCY, PAYMENT_TIME + 1, NAME, ACCRUAL_TIME, NOTIONAL);
    assertFalse(other.equals(COUPON));
    other = new MyCoupon(CCY, PAYMENT_TIME, NAME + "_", ACCRUAL_TIME, NOTIONAL);
    assertFalse(other.equals(COUPON));
    other = new MyCoupon(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME + 1, NOTIONAL);
    assertFalse(other.equals(COUPON));
    other = new MyCoupon(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL + 1);
    assertFalse(other.equals(COUPON));
  }

  private static class MyCoupon extends Coupon {

    public MyCoupon(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional) {
      super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    }

    @Override
    public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
      throw new NotImplementedException();
    }

    @Override
    public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
      throw new NotImplementedException();
    }

    @Override
    public Coupon withNotional(final double notional) {
      return null;
    }

  }
}
