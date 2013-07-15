/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class HullWhiteTwoFactorDataBundle extends StandardDiscountBondModelDataBundle {
  private final double _reversionSpeed1;
  private final double _reversionSpeed2;
  private final double _meanReversionLevel;
  private final YieldAndDiscountCurve _forwardRateCurve;
  private final VolatilityCurve _volatilityCurve2;
  private final double _correlation;

  public HullWhiteTwoFactorDataBundle(final YieldAndDiscountCurve shortRateCurve, final VolatilityCurve volatilityCurve1, final VolatilityCurve volatilityCurve2, final ZonedDateTime date,
      final double reversionSpeed1, final double reversionSpeed2, final double meanReversionLevel, final YieldAndDiscountCurve forwardRateCurve, final double correlation) {
    super(shortRateCurve, volatilityCurve1, date);
    Validate.notNull(volatilityCurve2, "second volatility curve");
    Validate.notNull(forwardRateCurve, "forward rate curve");
    if (!ArgumentChecker.isInRangeInclusive(-1, 1, correlation)) {
      throw new IllegalArgumentException("Correlation must be >= -1 and <= 1");
    }
    _reversionSpeed1 = reversionSpeed1;
    _reversionSpeed2 = reversionSpeed2;
    _meanReversionLevel = meanReversionLevel;
    _forwardRateCurve = forwardRateCurve;
    _volatilityCurve2 = volatilityCurve2;
    _correlation = correlation;
  }

  public double getFirstSpeed() {
    return _reversionSpeed1;
  }

  public double getSecondSpeed() {
    return _reversionSpeed2;
  }

  public double getMeanReversionLevel() {
    return _meanReversionLevel;
  }

  public double getForwardRate(final double t) {
    return _forwardRateCurve.getInterestRate(t);
  }

  public YieldAndDiscountCurve getForwardRateCurve() {
    return _forwardRateCurve;
  }

  public double getSecondVolatility(final double t) {
    return _volatilityCurve2.getVolatility(t);
  }

  public double getCorrelation() {
    return _correlation;
  }

  public VolatilityCurve getSecondVolatilityCurve() {
    return _volatilityCurve2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_correlation);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_forwardRateCurve == null) ? 0 : _forwardRateCurve.hashCode());
    temp = Double.doubleToLongBits(_meanReversionLevel);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_reversionSpeed1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_reversionSpeed2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_volatilityCurve2 == null) ? 0 : _volatilityCurve2.hashCode());
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
    final HullWhiteTwoFactorDataBundle other = (HullWhiteTwoFactorDataBundle) obj;
    if (Double.doubleToLongBits(_correlation) != Double.doubleToLongBits(other._correlation)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardRateCurve, other._forwardRateCurve)) {
      return false;
    }
    if (Double.doubleToLongBits(_meanReversionLevel) != Double.doubleToLongBits(other._meanReversionLevel)) {
      return false;
    }
    if (Double.doubleToLongBits(_reversionSpeed1) != Double.doubleToLongBits(other._reversionSpeed1)) {
      return false;
    }
    if (Double.doubleToLongBits(_reversionSpeed2) != Double.doubleToLongBits(other._reversionSpeed2)) {
      return false;
    }
    return ObjectUtils.equals(_volatilityCurve2, other._volatilityCurve2);
  }
}
