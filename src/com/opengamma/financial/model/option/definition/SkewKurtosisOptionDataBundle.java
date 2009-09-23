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
}
