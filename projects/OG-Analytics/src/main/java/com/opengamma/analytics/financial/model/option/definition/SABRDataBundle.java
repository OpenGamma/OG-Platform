/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SABRDataBundle extends StandardOptionDataBundle {
  private final double _alpha;
  private final double _beta;
  private final double _rho;
  private final double _volOfVol;

  public SABRDataBundle(final YieldAndDiscountCurve interestRateCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date, final double alpha,
      final double beta, final double rho, final double volOfVol) {
    super(interestRateCurve, b, volatilitySurface, spot, date);
    if (!ArgumentChecker.isInRangeInclusive(-1, 1, rho)) {
      throw new IllegalArgumentException("Correlation must be >= -1 and <= 1");
    }
    _alpha = alpha;
    _beta = beta;
    _rho = rho;
    _volOfVol = volOfVol;
  }

  public SABRDataBundle(final SABRDataBundle data) {
    super(data);
    _alpha = data.getAlpha();
    _beta = data.getBeta();
    _rho = data.getRho();
    _volOfVol = data.getVolOfVol();
  }

  public SABRDataBundle(final StandardOptionDataBundle data, final double alpha, final double beta, final double rho, final double volOfVol) {
    super(data);
    if (!ArgumentChecker.isInRangeInclusive(-1, 1, rho)) {
      throw new IllegalArgumentException("Correlation must be >= -1 and <= 1");
    }
    _alpha = alpha;
    _beta = beta;
    _rho = rho;
    _volOfVol = volOfVol;
  }

  public double getAlpha() {
    return _alpha;
  }

  public double getBeta() {
    return _beta;
  }

  public double getRho() {
    return _rho;
  }

  public double getVolOfVol() {
    return _volOfVol;
  }

  @Override
  public SABRDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new SABRDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAlpha(), getBeta(), getRho(), getVolOfVol());
  }

  @Override
  public SABRDataBundle withCostOfCarry(final double costOfCarry) {
    return new SABRDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getAlpha(), getBeta(), getRho(), getVolOfVol());
  }

  @Override
  public SABRDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new SABRDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getAlpha(), getBeta(), getRho(), getVolOfVol());
  }

  @Override
  public SABRDataBundle withDate(final ZonedDateTime date) {
    return new SABRDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getAlpha(), getBeta(), getRho(), getVolOfVol());
  }

  @Override
  public SABRDataBundle withSpot(final double spot) {
    return new SABRDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getAlpha(), getBeta(), getRho(), getVolOfVol());
  }

  public SABRDataBundle withAlpha(final double alpha) {
    return new SABRDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), alpha, getBeta(), getRho(), getVolOfVol());
  }

  public SABRDataBundle withBeta(final double beta) {
    return new SABRDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAlpha(), beta, getRho(), getVolOfVol());
  }

  public SABRDataBundle withRho(final double rho) {
    if (!ArgumentChecker.isInRangeInclusive(-1, 1, rho)) {
      throw new IllegalArgumentException("Correlation must be >= -1 and <= 1");
    }
    return new SABRDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAlpha(), getBeta(), rho, getVolOfVol());
  }

  public SABRDataBundle withVolOfVol(final double volOfVol) {
    return new SABRDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAlpha(), getBeta(), getRho(), volOfVol);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_alpha);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_beta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volOfVol);
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
    final SABRDataBundle other = (SABRDataBundle) obj;
    if (Double.doubleToLongBits(_alpha) != Double.doubleToLongBits(other._alpha)) {
      return false;
    }
    if (Double.doubleToLongBits(_beta) != Double.doubleToLongBits(other._beta)) {
      return false;
    }
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho)) {
      return false;
    }
    if (Double.doubleToLongBits(_volOfVol) != Double.doubleToLongBits(other._volOfVol)) {
      return false;
    }
    return true;
  }

}
