/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a standard deposit. The notional amount is paid on the start time and received back on the end (maturity) time with interests.
 * The interest amount is computed based on a fixed rate and a accrual factor.
 */
public class Cash implements InstrumentDerivative {

  /**
   * The deposit currency.
   */
  private final Currency _currency;
  /**
   * The deposit start time.
   */
  private final double _startTime;
  /**
   * The deposit end (or maturity) time.
   */
  private final double _endTime;
  /**
   * The deposit notional.
   */
  private final double _notional;
  /**
   * The accrual factor (or year fraction).
   */
  private final double _accrualFactor;
  /**
   * The deposit rate.
   */
  private final double _rate;
  /**
   * The interest amount to be paid at end date (=_notional * _rate * _accrualFactor)
   */
  private final double _interestAmount;
  /**
   * The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past.
   */
  private final double _initialAmount;
  private final String _discountingCurveName;

  /**
   * Constructor of a cash deposit.
   * @param currency The currency
   * @param startTime Time when the notional amount is borrowed (could be 0, i.e. now)
   * @param endTime Time from now (in years) when the loan matures (is repaid)
   * @param notional The notional of the loan
   * @param rate The loan rate.
   * @param accrualFactor The time (in years) between the start date and the end date in some day count convention.
   * @param yieldCurveName Name of yield curve used to price loan
   */
  public Cash(final Currency currency, final double startTime, final double endTime, final double notional, final double rate, final double accrualFactor, final String yieldCurveName) {
    this(currency, startTime, endTime, notional, notional, rate, accrualFactor, yieldCurveName);
  }

  /**
   * A cash loan
   * @param currency The currency
   * @param startTime Time when the notional amount is borrowed (could be 0, i.e. now)
   * @param endTime Time from now (in years) when the loan matures (is repaid)
   * @param notional The notional of the loan
   * @param initialAmount The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past. Should be of the same sign as notional.
   * @param rate The loan rate.
   * @param accrualFactor The time (in years) between the start date and the end date in some day count convention.
   * @param yieldCurveName Name of yield curve used to price loan
   */
  public Cash(final Currency currency, final double startTime, final double endTime, final double notional, final double initialAmount, final double rate, final double accrualFactor,
      final String yieldCurveName) {
    Validate.notNull(yieldCurveName);
    Validate.notNull(currency, "currency");
    Validate.isTrue(startTime >= 0, "Start time should be positive or 0.");
    Validate.isTrue(accrualFactor >= 0, "Accrual factor should be positive");
    Validate.isTrue(startTime <= endTime, "Start time must be less or equal to the end time"); //REVIEW: Should the time be restricted to startTime < endTime?
    Validate.isTrue(notional * initialAmount >= 0.0, "Notional and initial amount should have the same sign");
    _currency = currency;
    _endTime = endTime;
    _discountingCurveName = yieldCurveName;
    _startTime = startTime;
    _accrualFactor = accrualFactor;
    _rate = rate;
    _notional = notional;
    _interestAmount = _notional * _rate * _accrualFactor;
    _initialAmount = initialAmount;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public String getYieldCurveName() {
    return _discountingCurveName;
  }

  public double getStartTime() {
    return _startTime;
  }

  public double getEndTime() {
    return _endTime;
  }

  public double getAccrualFactor() {
    return _accrualFactor;
  }

  public double getRate() {
    return _rate;
  }

  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the interest amount.
   * @return The amount.
   */
  public double getInterestAmount() {
    return _interestAmount;
  }

  /**
   * Gets the initial amount.
   * @return The amount.
   */
  public double getInitialAmount() {
    return _initialAmount;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCash(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCash(this);
  }

  @Override
  public String toString() {
    return "Cash " + _currency.toString() + "[" + _startTime + " - " + _endTime + "], r = " + _rate + ", notional = " + _notional + "curve = " + _discountingCurveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _discountingCurveName.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_startTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_accrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Cash other = (Cash) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_discountingCurveName, other._discountingCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_startTime) != Double.doubleToLongBits(other._startTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_accrualFactor) != Double.doubleToLongBits(other._accrualFactor)) {
      return false;
    }
    return true;
  }
}
