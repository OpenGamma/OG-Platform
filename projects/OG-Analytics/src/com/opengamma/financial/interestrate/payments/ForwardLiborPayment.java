/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ForwardLiborPayment implements IndexPayment {

  private final double _paymentTime;
  private final double _paymentYearFraction;
  private final double _forwardYearFraction;
  private final double _liborFixingTime;
  private final double _liborMaturityTime;
  private final double _spread;
  private final String _curveName;

  public ForwardLiborPayment(double paymentTime, double liborFixingTime, double liborMaturityTime, double paymentYearFraction, double forwardYearFraction, String curveName) {
    this(paymentTime, liborFixingTime, liborMaturityTime, paymentYearFraction, forwardYearFraction, 0.0, curveName);
  }

  public ForwardLiborPayment(double paymentTime, double liborFixingTime, double liborMaturityTime, double paymentYearFraction, double forwardYearFraction, double spread, String curveName) {

    Validate.isTrue(paymentTime >= 0.0, "payment time < 0");
    Validate.isTrue(liborFixingTime <= paymentTime, "libor Fixing > payement time");
    Validate.isTrue(liborMaturityTime > liborFixingTime, "libor maturity < fixing time");
    Validate.isTrue(paymentYearFraction > 0, "payment year fraction <=0");
    Validate.isTrue(forwardYearFraction > 0, "forward year fraction <=0");
    double actActYearFrac = liborMaturityTime - liborFixingTime;
    // TODO try to catch a wrongly entered year fraction with very lose bounds around the ACT/ACT value (i.e. maturity - fixing). Needs more thought as to whether this should be tightened or removed
    Validate.isTrue(paymentYearFraction < 1.1 * paymentYearFraction + 0.1 && paymentYearFraction > 0.9 * paymentYearFraction - 0.1, "input year fraction is " + paymentYearFraction
        + " but ACT/ACT value is " + actActYearFrac);
    _paymentTime = paymentTime;
    _liborFixingTime = liborFixingTime;
    _liborMaturityTime = liborMaturityTime;
    _paymentYearFraction = paymentYearFraction;
    _forwardYearFraction = forwardYearFraction;
    _spread = spread;
    _curveName = curveName;
  }

  @Override
  public String getIndexCurveName() {
    return _curveName;
  }

  @Override
  public double getPaymentTime() {
    return _paymentTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curveName == null) ? 0 : _curveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_forwardYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_liborFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_liborMaturityTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ForwardLiborPayment other = (ForwardLiborPayment) obj;
    if (_curveName == null) {
      if (other._curveName != null) {
        return false;
      }
    } else if (!_curveName.equals(other._curveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_forwardYearFraction) != Double.doubleToLongBits(other._forwardYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_liborFixingTime) != Double.doubleToLongBits(other._liborFixingTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_liborMaturityTime) != Double.doubleToLongBits(other._liborMaturityTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentYearFraction) != Double.doubleToLongBits(other._paymentYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

  /**
   * Gets the year fraction used to calculated the payment amount, i.e payment = paymentYearFraction*(liborRate + spread).
   * @return the yearFraction
   */
  public double getPaymentYearFraction() {
    return _paymentYearFraction;
  }

  /**
   * Gets the year fraction used to calculate the forward libor rate, i.e. forward = (p(fixing)/p(maturity)-1)/forwardYearFraction where p(fixing) 
   * and p(maturity) are the discount factors at fixing and maturity
   * @return the forwardYearFraction
   */
  public double getForwardYearFraction() {
    return _forwardYearFraction;
  }

  /**
   * Gets the liborFixingTime field.
   * @return the liborFixingTime
   */
  public double getLiborFixingTime() {
    return _liborFixingTime;
  }

  /**
   * Gets the liborMaturityTime field.
   * @return the liborMaturityTime
   */
  public double getLiborMaturityTime() {
    return _liborMaturityTime;
  }

  /**
   * Gets the spread field.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }

  @Override
  public <S, T> T accept(PaymentVisitor<S, T> visitor, S data) {
    return visitor.visitForwardLiborPayment(this, data);
  }

}
