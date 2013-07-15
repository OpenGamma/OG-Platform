/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class VasicekDataBundle extends StandardDiscountBondModelDataBundle {
  private final double _longTermInterestRate;
  private final double _reversionSpeed;

  public VasicekDataBundle(final YieldAndDiscountCurve shortRateCurve, final VolatilityCurve shortRateVolatilityCurve, final ZonedDateTime date, final double longTermInterestRate,
      final double reversionSpeed) {
    super(shortRateCurve, shortRateVolatilityCurve, date);
    ArgumentChecker.notZero(reversionSpeed, 1e-15, "reversion speed");
    _longTermInterestRate = longTermInterestRate;
    _reversionSpeed = reversionSpeed;
  }

  public double getLongTermInterestRate() {
    return _longTermInterestRate;
  }

  public double getReversionSpeed() {
    return _reversionSpeed;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_longTermInterestRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_reversionSpeed);
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
    final VasicekDataBundle other = (VasicekDataBundle) obj;
    if (Double.doubleToLongBits(_longTermInterestRate) != Double.doubleToLongBits(other._longTermInterestRate)) {
      return false;
    }
    return Double.doubleToLongBits(_reversionSpeed) == Double.doubleToLongBits(other._reversionSpeed);
  }
}
