package com.opengamma.financial.model.option.definition;

import javax.time.InstantProvider;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 * @author emcleod
 */
public class HullWhiteStochasticVolatilityModelOptionDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _sigmaLR;
  private final double _volOfSigma;
  private final double _rho;

  public HullWhiteStochasticVolatilityModelOptionDataBundle(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, InstantProvider date,
      double lambda, double sigmaLR, double volOfSigma, double rho) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _lambda = lambda;
    _sigmaLR = sigmaLR;
    _volOfSigma = volOfSigma;
    _rho = rho;
  }

  public double getHalfLife() {
    return _lambda;
  }

  public double getLongRunVolatility() {
    return _sigmaLR;
  }

  public double getVolatilityOfVolatility() {
    return _volOfSigma;
  }

  public double getCorrelation() {
    return _rho;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sigmaLR);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volOfSigma);
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
    HullWhiteStochasticVolatilityModelOptionDataBundle other = (HullWhiteStochasticVolatilityModelOptionDataBundle) obj;
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda))
      return false;
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho))
      return false;
    if (Double.doubleToLongBits(_sigmaLR) != Double.doubleToLongBits(other._sigmaLR))
      return false;
    if (Double.doubleToLongBits(_volOfSigma) != Double.doubleToLongBits(other._volOfSigma))
      return false;
    return true;
  }
}
