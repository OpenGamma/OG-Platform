package com.opengamma.financial.model.option.definition;

import java.util.Date;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

public class StandardOptionDataBundle {
  private final DiscountCurve _discountCurve;
  private final double _b;
  private final VolatilitySurface _volatilitySurface;
  private final double _spot;
  private final Date _date;

  // TODO probably need a cost of carry model
  public StandardOptionDataBundle(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, Date date) {
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

  public Date getDate() {
    return _date;
  }
}
