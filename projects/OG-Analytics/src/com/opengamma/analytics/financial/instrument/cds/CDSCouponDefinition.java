/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CDSCouponDefinition extends CouponFixedDefinition {
  
  private static DayCount s_act365 = new ActualThreeSixtyFive();

  public CDSCouponDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentYearFraction,
      final double notional, final double rate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, rate);
  }
  
  @Override
  public CouponFixed toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date"); // Required: reference date <= payment date
    
    final String fundingCurveName = yieldCurveNames[0];
    final double paymentTime = date.isBefore(getPaymentDate()) ? s_act365.getDayCountFraction(date, getPaymentDate()) : s_act365.getDayCountFraction(getPaymentDate(), date); 
    
    return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), getRate(), getAccrualStartDate(), getAccrualEndDate());
  }
}
