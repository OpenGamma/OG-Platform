/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.credit.cds.ISDACDSCoupon;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CDSCouponDefinition extends CouponFixedDefinition {
  
  private static final DayCount ACT_365F = new ActualThreeSixtyFive();

  public CDSCouponDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentYearFraction,
      final double notional, final double rate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, rate);
  }
  
  @Override
  public ISDACDSCoupon toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date"); // Required: reference date <= payment date
    
    final String fundingCurveName = yieldCurveNames[0]; 
    
    return new ISDACDSCoupon(getCurrency(), getTimeBetween(date, getPaymentDate()), fundingCurveName, getPaymentYearFraction(), getNotional(), getRate(),
      getAccrualStartDate(), getAccrualEndDate(), getTimeBetween(date, getAccrualStartDate()), getTimeBetween(date, getAccrualEndDate()));
  }
  
  private static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {
    
    return date2.isBefore(date1)
      ? -ACT_365F.getDayCountFraction(date2, date1)
      :  ACT_365F.getDayCountFraction(date1, date2);
  }
}
