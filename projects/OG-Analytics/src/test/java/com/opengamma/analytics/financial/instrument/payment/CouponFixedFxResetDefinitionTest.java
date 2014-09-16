/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the constructors and equal/hash for CouponFixedFxResetDefinition.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedFxResetDefinitionTest {
  
  /** Details coupon. */
  private static final Currency CUR_REF = Currency.EUR;
  private static final Currency CUR_PAY = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final ZonedDateTime FX_FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double ACCRUAL_FACTOR = 0.267;
  private static final double NOTIONAL = 1000000; //1m
  private static final double RATE = 0.04;
  
  private static final CouponFixedFxResetDefinition CPN = new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, 
      ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, RATE, CUR_REF, FX_FIXING_DATE);
  
  private static final double FX_FIXING_RATE = 1.40;
  private static final DoubleTimeSeries<ZonedDateTime> FX_FIXING_TS_10 = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {FX_FIXING_DATE.minusDays(11), FX_FIXING_DATE.minusDays(10) }, 
          new double[] {1.38, 1.39 });
  private static final DoubleTimeSeries<ZonedDateTime> FX_FIXING_TS_1 = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {FX_FIXING_DATE.minusDays(2), FX_FIXING_DATE.minusDays(1) }, 
          new double[] {1.38, 1.39 });
  private static final DoubleTimeSeries<ZonedDateTime> FX_FIXING_TS0 = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {FX_FIXING_DATE.minusDays(2), FX_FIXING_DATE.minusDays(1), FX_FIXING_DATE }, 
          new double[] {1.38, 1.39, FX_FIXING_RATE });  

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullReferenceCurrency() {
    new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, 
        NOTIONAL, RATE, null, FX_FIXING_DATE);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingDate() {
    new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, 
        NOTIONAL, RATE, CUR_REF, null);
  }
  
  @Test
  public void getter() {
    assertEquals("CouponFixedFxResetDefinition: getter", RATE, CPN.getRate());
    assertEquals("CouponFixedFxResetDefinition: getter", CUR_REF, CPN.getReferenceCurrency());
    assertEquals("CouponFixedFxResetDefinition: getter", FX_FIXING_DATE, CPN.getFxFixingDate());
  }


  @Test
  public void equalHash() {
    assertEquals("CouponFixedFxResetDefinition: hash-equal", CPN, CPN);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(CUR_REF));
    CouponFixedFxResetDefinition other = new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, 
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, RATE, CUR_REF, FX_FIXING_DATE);
    assertEquals("CouponFixedFxResetDefinition: hash-equal", CPN, other);
    assertEquals("CouponFixedFxResetDefinition: hash-equal", CPN.hashCode(), other.hashCode());
    CouponFixedFxResetDefinition modified;
    modified = new CouponFixedFxResetDefinition(Currency.AUD, PAYMENT_DATE, 
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, RATE, CUR_REF, FX_FIXING_DATE);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
    modified = new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, 
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, RATE+0.01, CUR_REF, FX_FIXING_DATE);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
    modified = new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, 
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, RATE, Currency.AUD, FX_FIXING_DATE);
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
    modified = new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, 
        ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, RATE, CUR_REF, FX_FIXING_DATE.plusDays(1));
    assertFalse("CouponFixedFxResetDefinition: hash-equal", CPN.equals(modified));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toDerivativeMissingFixing() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.plusDays(1);
    CPN.toDerivative(valuationDate);
  }

  @Test
  public void toDerivativeBeforeFixingNoHts() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.minusDays(10);
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    CouponFixedFxReset cpnExpected = new CouponFixedFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, RATE, 
       CUR_REF, fixingTime);    
    CouponFixedFxReset cpnConverted = CPN.toDerivative(valuationDate);
    assertEquals("CouponFixedFxResetDefinition: toDerivative", cpnExpected, cpnConverted);
  }  
  
  @Test
  public void toDerivativeBeforeFixing() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.minusDays(10);
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    CouponFixedFxReset cpnExpected = new CouponFixedFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, RATE, 
       CUR_REF, fixingTime);    
    Payment cpnConverted = CPN.toDerivative(valuationDate, FX_FIXING_TS_10);
    assertEquals("CouponFixedFxResetDefinition: toDerivative", cpnExpected, cpnConverted);     
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void toDerivativeAfterFixingNotAvailable() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.plusDays(1);
    CPN.toDerivative(valuationDate, FX_FIXING_TS_1);
  } 
  
  @Test
  public void toDerivativeOnFixingNotAvailable() {
    ZonedDateTime valuationDate = FX_FIXING_DATE;
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    double fixingTime = TimeCalculator.getTimeBetween(valuationDate, FX_FIXING_DATE);
    CouponFixedFxReset cpnExpected = new CouponFixedFxReset(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL, RATE, 
       CUR_REF, fixingTime);    
    Payment cpnConverted = CPN.toDerivative(valuationDate, FX_FIXING_TS_1);
    assertEquals("CouponFixedFxResetDefinition: toDerivative", cpnExpected, cpnConverted);     
  }
  
  @Test
  public void toDerivativeOnFixingAvailable() {
    ZonedDateTime valuationDate = FX_FIXING_DATE;
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    CouponFixed cpnExpected = new CouponFixed(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL*FX_FIXING_RATE, RATE);
    Payment cpnConverted = CPN.toDerivative(valuationDate, FX_FIXING_TS0);
    assertEquals("CouponFixedFxResetDefinition: toDerivative", cpnExpected, cpnConverted);     
  }
  
  @Test
  public void toDerivativeAfterFixing() {
    ZonedDateTime valuationDate = FX_FIXING_DATE.plusDays(1);
    double paymentTime = TimeCalculator.getTimeBetween(valuationDate, PAYMENT_DATE);
    CouponFixed cpnExpected = new CouponFixed(CUR_PAY, paymentTime, ACCRUAL_FACTOR, NOTIONAL*FX_FIXING_RATE, RATE);
    Payment cpnConverted = CPN.toDerivative(valuationDate, FX_FIXING_TS0);
    assertEquals("CouponFixedFxResetDefinition: toDerivative", cpnExpected, cpnConverted);     
  }
  
}
