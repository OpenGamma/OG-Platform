package com.opengamma.financial.model.option.definition;

import java.util.Date;

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

  public HullWhiteStochasticVolatilityModelOptionDataBundle(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, Date date, double lambda, double sigmaLR,
      double volOfSigma, double rho) {
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
}
