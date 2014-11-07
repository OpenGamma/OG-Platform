/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFloatingDefinitionTest {

  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m

  private static final ZonedDateTime FAKE_DATE = DateUtils.getUTCDate(0, 1, 1);
  private static final CouponFloatingDefinition COUPON = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE);
  private static final CouponFloatingDefinition FLOAT_COUPON = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new MyCouponFloatingDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new MyCouponFloatingDefinition(CUR, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null);
  }

  @Test
  public void test() {
    assertEquals(FLOAT_COUPON.getCurrency(), CUR);
    assertEquals(FLOAT_COUPON.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(FLOAT_COUPON.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(FLOAT_COUPON.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(FLOAT_COUPON.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(FLOAT_COUPON.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(FLOAT_COUPON.getFixingDate(), FIXING_DATE);
    CouponFloatingDefinition other = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);
    assertEquals(FLOAT_COUPON, other);
    assertEquals(FLOAT_COUPON.hashCode(), other.hashCode());
    other = new MyCouponFloatingDefinition(Currency.AUD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);
    assertFalse(FLOAT_COUPON.equals(other));
    other = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE.plusDays(1), ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);
    assertFalse(FLOAT_COUPON.equals(other));
    other = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE.plusDays(1), ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);
    assertFalse(FLOAT_COUPON.equals(other));
    other = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE.plusDays(1), ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE);
    assertFalse(FLOAT_COUPON.equals(other));
    other = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR + 0.01, NOTIONAL, FIXING_DATE);
    assertFalse(FLOAT_COUPON.equals(other));
    other = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL + 1000, FIXING_DATE);
    assertFalse(FLOAT_COUPON.equals(other));
    other = new MyCouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE.plusDays(1));
    assertFalse(FLOAT_COUPON.equals(other));
  }

  private static class MyCouponFloatingDefinition extends CouponFloatingDefinition {

    public MyCouponFloatingDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
        final double notional, final ZonedDateTime fixingDate) {
      super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    }

    @Override
    public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data) {
      throw new NotImplementedException();
    }

    @Override
    public Payment toDerivative(final ZonedDateTime date) {
      throw new NotImplementedException();
    }

    @Override
    public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
