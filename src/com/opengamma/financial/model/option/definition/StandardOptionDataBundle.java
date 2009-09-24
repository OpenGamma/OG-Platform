package com.opengamma.financial.model.option.definition;

import javax.time.InstantProvider;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

public class StandardOptionDataBundle {
  private final DiscountCurve _discountCurve;
  private final double _b;
  private final VolatilitySurface _volatilitySurface;
  private final double _spot;
  private final InstantProvider _date;

  // TODO probably need a cost of carry model
  public StandardOptionDataBundle(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, InstantProvider date) {
    _discountCurve = discountCurve;
    _b = b;
    _volatilitySurface = volatilitySurface;
    _spot = spot;
    _date = date;
  }

  public double getInterestRate(Double t) {
    return _discountCurve.getInterestRate(t);
  }

  public double getCostOfCarry() {
    return _b;
  }

  public Double getVolatility(Double t, Double strike) {
    return _volatilitySurface.getVolatility(t, strike);
  }

  public double getSpot() {
    return _spot;
  }

  public DiscountCurve getDiscountCurve() {
    return _discountCurve;
  }

  public VolatilitySurface getVolatilitySurface() {
    return _volatilitySurface;
  }

  public InstantProvider getDate() {
    return _date;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_b);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_date == null) ? 0 : _date.hashCode());
    result = prime * result + ((_discountCurve == null) ? 0 : _discountCurve.hashCode());
    temp = Double.doubleToLongBits(_spot);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_volatilitySurface == null) ? 0 : _volatilitySurface.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StandardOptionDataBundle other = (StandardOptionDataBundle) obj;
    if (Double.doubleToLongBits(_b) != Double.doubleToLongBits(other._b))
      return false;
    if (_date == null) {
      if (other._date != null)
        return false;
    } else if (!_date.equals(other._date))
      return false;
    if (_discountCurve == null) {
      if (other._discountCurve != null)
        return false;
    } else if (!_discountCurve.equals(other._discountCurve))
      return false;
    if (Double.doubleToLongBits(_spot) != Double.doubleToLongBits(other._spot))
      return false;
    if (_volatilitySurface == null) {
      if (other._volatilitySurface != null)
        return false;
    } else if (!_volatilitySurface.equals(other._volatilitySurface))
      return false;
    return true;
  }
}
