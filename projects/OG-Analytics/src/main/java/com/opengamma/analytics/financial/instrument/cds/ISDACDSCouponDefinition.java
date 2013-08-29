/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACDSCoupon;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * ISDA definition for a CDS coupon.
 *
 * This is structurally identical to the standard definition for a fixed coupon, with the
 * exception that when it is converted to a derivative object an {@link ISDACDSCoupon} is
 * produced rather than a {@link CouponFixed}. The start time, end time and payment time
 * for the accrual period of the ISDACDSCoupon are computed using ACT/365F relative to
 * the pricing point, in accordance with the ISDA model. The payment year fraction must
 * be computed according to the conventions describing the particular CDS contract (this
 * is the responsibility of {@link ISDACDSPremiumDefinition}).
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see CouponFixedDefinition
 * @see ISDACDSPremiumDefinition
 */
public class ISDACDSCouponDefinition extends CouponFixedDefinition {

  private static final DayCount ACT_365F = new ActualThreeSixtyFive();

  public ISDACDSCouponDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentYearFraction,
      final double notional, final double rate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, rate);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not refer to yield curve names
   */
  @Deprecated
  @Override
  public ISDACDSCoupon toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {

    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date"); // Required: reference date <= payment date

    final String fundingCurveName = yieldCurveNames[0];

    return new ISDACDSCoupon(getCurrency(), getTimeBetween(date, getPaymentDate()), fundingCurveName, getPaymentYearFraction(), getNotional(), getRate(),
        getAccrualStartDate(), getAccrualEndDate(), getTimeBetween(date, getAccrualStartDate()), getTimeBetween(date, getAccrualEndDate()));
  }

  @Override
  public ISDACDSCoupon toDerivative(final ZonedDateTime date) {

    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date"); // Required: reference date <= payment date

    return new ISDACDSCoupon(getCurrency(), getTimeBetween(date, getPaymentDate()), getPaymentYearFraction(), getNotional(), getRate(),
        getAccrualStartDate(), getAccrualEndDate(), getTimeBetween(date, getAccrualStartDate()), getTimeBetween(date, getAccrualEndDate()));
  }

  private static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {

    final ZonedDateTime rebasedDate2 = date2.withZoneSameInstant(date1.getZone());

    return rebasedDate2.isBefore(date1)
      ? -ACT_365F.getDayCountFraction(rebasedDate2, date1)
      :  ACT_365F.getDayCountFraction(date1, rebasedDate2);
  }
}
