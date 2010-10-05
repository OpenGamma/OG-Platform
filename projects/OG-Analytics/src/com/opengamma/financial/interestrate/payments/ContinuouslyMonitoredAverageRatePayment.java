/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

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

  public ContinuouslyMonitoredAverageRatePayment(double paymentTime, double notional, double startTime, double endTime, double paymentYearFraction, double rateYearFraction, double spread,
      String fundingCurve, String indexCurve) {

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
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitContinuouslyMonitoredAverageRatePayment(this, data);
  }

}
