/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cds.ISDACDSCouponDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSPremiumDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.util.money.Currency;

/**
 * Represent a CDS coupon payment as understood by the ISDA standard model.
 * 
 * The payment year fraction, accrual period start and end times and the payment time
 * must be computed using day counts and conventions to match the ISDA standard model.
 * These calculations are the responsibility of {@link ISDACDSCouponDefinition} and
 * {@link ISDACDSPremiumDefinition}.
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see CouponFixed
 * @see ISDACDSDerivative
 * @deprecated Use classes from isdastandardmodel
 */
@Deprecated
public class ISDACDSCoupon extends CouponFixed {

  private final double _accrualStartTime;

  private final double _accrualEndTime;

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   * @param accrualStartDate The start date of the coupon accrual period.
   * @param accrualEndDate The end date of the coupon accrual period.
   * @param accrualStartTime The start time of the coupon accrual period.
   * @param accrualEndTime The end time of the coupon accrual period.
   * @deprecated Use the constructor that does not take curve names
   */
  @Deprecated
  public ISDACDSCoupon(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double rate,
      final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualStartTime, final double accrualEndTime) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, rate, accrualStartDate, accrualEndDate);

    _accrualStartTime = accrualStartTime;
    _accrualEndTime = accrualEndTime;
  }

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   * @param accrualStartDate The start date of the coupon accrual period.
   * @param accrualEndDate The end date of the coupon accrual period.
   * @param accrualStartTime The start time of the coupon accrual period.
   * @param accrualEndTime The end time of the coupon accrual period.
   */
  public ISDACDSCoupon(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double rate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double accrualStartTime, final double accrualEndTime) {
    super(currency, paymentTime, paymentYearFraction, notional, rate, accrualStartDate, accrualEndDate);

    _accrualStartTime = accrualStartTime;
    _accrualEndTime = accrualEndTime;
  }

  /**
   * Gets the start time of the coupon accrual period.
   * @return The accrual start time
   */
  public double getAccrualStartTime() {
    return _accrualStartTime;
  }

  /**
   * Gets the end time of the coupon accrual period.
   * @return The accrual end time
   */
  public double getAccrualEndTime() {
    return _accrualEndTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_accrualStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_accrualEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ISDACDSCoupon other = (ISDACDSCoupon) obj;
    if (Double.doubleToLongBits(_accrualStartTime) != Double.doubleToLongBits(other._accrualStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_accrualEndTime) != Double.doubleToLongBits(other._accrualEndTime)) {
      return false;
    }
    return true;
  }
}
