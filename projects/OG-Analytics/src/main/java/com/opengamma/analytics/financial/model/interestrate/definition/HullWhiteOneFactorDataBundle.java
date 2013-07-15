/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;

/**
 * 
 */
public class HullWhiteOneFactorDataBundle extends StandardDiscountBondModelDataBundle {
  private final double _reversionSpeed;

  public HullWhiteOneFactorDataBundle(final YieldAndDiscountCurve shortRateCurve, final VolatilityCurve shortRateVolatilityCurve, final ZonedDateTime date, final double reversionSpeed) {
    super(shortRateCurve, shortRateVolatilityCurve, date);
    _reversionSpeed = reversionSpeed;
  }

  public double getReversionSpeed() {
    return _reversionSpeed;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
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
    final HullWhiteOneFactorDataBundle other = (HullWhiteOneFactorDataBundle) obj;
    if (Double.doubleToLongBits(_reversionSpeed) != Double.doubleToLongBits(other._reversionSpeed)) {
      return false;
    }
    return true;
  }

}
