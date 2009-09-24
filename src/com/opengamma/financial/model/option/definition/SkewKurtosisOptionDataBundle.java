package com.opengamma.financial.model.option.definition;

import javax.time.InstantProvider;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 * @author emcleod
 */
public class SkewKurtosisOptionDataBundle extends StandardOptionDataBundle {
  private final double _skew;
  private final double _kurtosis;

  public SkewKurtosisOptionDataBundle(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, InstantProvider date, double skew, double kurtosis) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _skew = skew;
    _kurtosis = kurtosis;
  }

  public double getSkew() {
    return _skew;
  }

  public double getKurtosis() {
    return _kurtosis;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_kurtosis);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_skew);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SkewKurtosisOptionDataBundle other = (SkewKurtosisOptionDataBundle) obj;
    if (Double.doubleToLongBits(_kurtosis) != Double.doubleToLongBits(other._kurtosis))
      return false;
    if (Double.doubleToLongBits(_skew) != Double.doubleToLongBits(other._skew))
      return false;
    return true;
  }
}
