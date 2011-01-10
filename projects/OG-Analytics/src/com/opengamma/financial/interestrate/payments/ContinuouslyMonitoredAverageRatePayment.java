/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;

/**
 * Continuously monitored 
 */
public class ContinuouslyMonitoredAverageRatePayment implements Payment {
  private final double _paymentTime;
  private final double _paymentYearFraction;
  private final double _rateYearFraction;
  private final double _startTime;
  private final double _endTime;
  private final double _spread;
  private final double _notional;
  private final String _fundingCurveName;
  private final String _indexCurveName;

  public ContinuouslyMonitoredAverageRatePayment(final double paymentTime, final double notional, final double startTime, final double endTime, final double paymentYearFraction,
      final double rateYearFraction, final double spread, final String fundingCurve, final String indexCurve) {
    Validate.isTrue(paymentTime > 0, "paymentTime <= 0");
    Validate.isTrue(startTime >= 0, "startTime < 0");
    Validate.isTrue(endTime > startTime && endTime <= paymentTime, "endTime < startTime or endTime > paymentTime");
    Validate.notNull(fundingCurve);
    Validate.notNull(indexCurve);
    _paymentTime = paymentTime;
    _notional = notional;
    _startTime = startTime;
    _endTime = endTime;
    _paymentYearFraction = paymentYearFraction;
    _rateYearFraction = rateYearFraction;
    _spread = spread;
    _fundingCurveName = fundingCurve;
    _indexCurveName = indexCurve;
  }

  @Override
  public String getFundingCurveName() {
    return _fundingCurveName;
  }

  @Override
  public double getPaymentTime() {
    return _paymentTime;
  }

  /**
   * Gets the paymentYearFraction field.
   * @return the paymentYearFraction
   */
  public double getPaymentYearFraction() {
    return _paymentYearFraction;
  }

  /**
   * Gets the rateYearFraction field.
   * @return the rateYearFraction
   */
  public double getRateYearFraction() {
    return _rateYearFraction;
  }

  /**
   * Gets the startTime field.
   * @return the startTime
   */
  public double getStartTime() {
    return _startTime;
  }

  /**
   * Gets the endTime field.
   * @return the endTime
   */
  public double getEndTime() {
    return _endTime;
  }

  /**
   * Gets the spread field.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the notional field.
   * @return the notional
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the indexCurveName field.
   * @return the indexCurveName
   */
  public String getIndexCurveName() {
    return _indexCurveName;
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitContinuouslyMonitoredAverageRatePayment(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitContinuouslyMonitoredAverageRatePayment(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fundingCurveName.hashCode();
    result = prime * result + _indexCurveName.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rateYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_startTime);
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
    final ContinuouslyMonitoredAverageRatePayment other = (ContinuouslyMonitoredAverageRatePayment) obj;
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingCurveName, other._fundingCurveName)) {
      return false;
    }
    if (!ObjectUtils.equals(_indexCurveName, other._indexCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentYearFraction) != Double.doubleToLongBits(other._paymentYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_rateYearFraction) != Double.doubleToLongBits(other._rateYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return Double.doubleToLongBits(_startTime) == Double.doubleToLongBits(other._startTime);
  }

}
