/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSCoupon;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;

/**
 * 
 */
public class CouponOnlyElement {

  private final double _riskLessValue;
  private final double _effEnd;
  private final int _creditCurveKnot;

  public CouponOnlyElement(final CDSCoupon coupon, final ISDACompliantYieldCurve yieldCurve, final int creditCurveKnot) {
    _riskLessValue = coupon.getYearFrac() * yieldCurve.getDiscountFactor(coupon.getPaymentTime());
    _effEnd = coupon.getEffEnd();
    _creditCurveKnot = creditCurveKnot;
  }

  public double pv(final ISDACompliantCreditCurve creditCurve) {
    return _riskLessValue * creditCurve.getDiscountFactor(_effEnd);
  }

  public double[] pvAndSense(final ISDACompliantCreditCurve creditCurve) {
    final double pv = _riskLessValue * creditCurve.getDiscountFactor(_effEnd);
    final double pvSense = -pv * creditCurve.getSingleNodeRTSensitivity(_effEnd, _creditCurveKnot);
    return new double[] {pv, pvSense };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _creditCurveKnot;
    long temp;
    temp = Double.doubleToLongBits(_effEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_riskLessValue);
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
    final CouponOnlyElement other = (CouponOnlyElement) obj;
    if (_creditCurveKnot != other._creditCurveKnot) {
      return false;
    }
    if (Double.doubleToLongBits(_effEnd) != Double.doubleToLongBits(other._effEnd)) {
      return false;
    }
    if (Double.doubleToLongBits(_riskLessValue) != Double.doubleToLongBits(other._riskLessValue)) {
      return false;
    }
    return true;
  }
}
