/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * The OIS rate the the continuously compounded overnight rate (e.g. Feb Funds for USD) from the start time (T) to the end time (S) 
 */
public class ZZZCouponOIS extends Coupon {
  private final double _rateYearFraction;
  private final double _startTime;
  private final double _endTime;
  private final double _spread;
  private final String _indexCurveName;

  public ZZZCouponOIS(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double notional, double rateYearFraction, double startTime, double endTime,
      double spread, String indexCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    Validate.isTrue(startTime >= 0, "startTime < 0");
    Validate.isTrue(endTime > startTime && endTime <= paymentTime, "endTime < startTime or endTime > paymentTime");
    Validate.notNull(indexCurveName);
    _startTime = startTime;
    _endTime = endTime;
    _rateYearFraction = rateYearFraction;
    _spread = spread;
    _indexCurveName = indexCurveName;
  }

  /**
   * Gets the _rateYearFraction field.
   * @return the _rateYearFraction
   */
  public double getRateYearFraction() {
    return _rateYearFraction;
  }

  /**
   * Gets the _startTime field.
   * @return the _startTime
   */
  public double getStartTime() {
    return _startTime;
  }

  /**
   * Gets the _endTime field.
   * @return the _endTime
   */
  public double getEndTime() {
    return _endTime;
  }

  /**
   * Gets the _spread field.
   * @return the _spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the _indexCurveName field.
   * @return the _indexCurveName
   */
  public String getIndexCurveName() {
    return _indexCurveName;
  }

  @Override
  public ZZZCouponOIS withNotional(double notional) {
    return new ZZZCouponOIS(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getRateYearFraction(), _startTime, _endTime, _spread, getIndexCurveName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _indexCurveName.hashCode();
    temp = Double.doubleToLongBits(_rateYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_startTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ZZZCouponOIS other = (ZZZCouponOIS) obj;
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_indexCurveName, other._indexCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_rateYearFraction) != Double.doubleToLongBits(other._rateYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (Double.doubleToLongBits(_startTime) != Double.doubleToLongBits(other._startTime)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitZZZCouponOIS(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitZZZCouponOIS(this);
  }
}
