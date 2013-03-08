/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.math.surface.Surface;

/**
 *  Describes a partial differential for a option price V in the form
 *  $\frac{\partial V}{\partial t} + a(S,t) \frac{\partial^2 V}{\partial S^2}$ + b(S,t) \frac{\partial V}{\partial S} + (S,t)V = 0$
 */
public class PDEOptionDataBundle extends StandardOptionDataBundle {

  private final Surface<Double, Double, Double> _a;
  private final Surface<Double, Double, Double> _b;
  private final Surface<Double, Double, Double> _c;

  public PDEOptionDataBundle(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b, final Surface<Double, Double, Double> c, final double spot, final ZonedDateTime date) {
    super(null, 0.0, null, spot, date);
    Validate.notNull(a, "null a");
    Validate.notNull(b, "null b");
    Validate.notNull(c, "null c");
    _a = a;
    _b = b;
    _c = c;
  }

  public double getA(final double f, final double t) {
    return _a.getZValue(f, t);
  }

  public double getB(final double f, final double t) {
    return _b.getZValue(f, t);
  }

  public double getC(final double f, final double t) {
    return _c.getZValue(f, t);
  }

  @Override
  public PDEOptionDataBundle withSpot(final double spot) {
    return new PDEOptionDataBundle(_a, _b, _c, spot, getDate());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _a.hashCode();
    result = prime * result + _b.hashCode();
    result = prime * result + _c.hashCode();
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
    if (!(obj instanceof PDEOptionDataBundle)) {
      return false;
    }
    final PDEOptionDataBundle other = (PDEOptionDataBundle) obj;
    if (!ObjectUtils.equals(_a, other._a)) {
      return false;
    }
    if (!ObjectUtils.equals(_b, other._b)) {
      return false;
    }
    if (!ObjectUtils.equals(_c, other._c)) {
      return false;
    }
    return true;
  }

}
