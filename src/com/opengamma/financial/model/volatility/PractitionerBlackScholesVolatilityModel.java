package com.opengamma.financial.model.volatility;

import java.util.Date;
import java.util.Map;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.regression.LeastSquaresRegression;

public class PractitionerBlackScholesVolatilityModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition> {
  private final VolatilitySurfaceModel<EuropeanVanillaOptionDefinition> _bsmVolatilityModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  private LeastSquaresRegression _regression;

  @Override
  public VolatilitySurface getSurface(EuropeanVanillaOptionDefinition definition, Map<EuropeanVanillaOptionDefinition, Object[]> data, Date date) {
    /*
     * double[][] x = new double[data.size()][6]; double[] y = new
     * double[data.size()]; int i = 0; double k; double t; for
     * (EuropeanVanillaOptionDefinition key : data.keySet()) { x[i][0] = 1; k =
     * key.getStrike(); t = key.getTimeToExpiry(date); x[i][1] = k; x[i][2] = k
     * * k; x[i][3] = t; x[i][4] = t * t; x[i][5] = k * t; y[i] =
     * _bsmVolatilityModel.getSurface(definition,
     * Collections.<EuropeanVanillaOptionDefinition, Object[]> singletonMap(key,
     * data.get(key)), date).getVolatility(0., 0.); } k =
     * definition.getStrike(); t = definition.getTimeToExpiry(date); double[] a
     * = _regression.getCoefficients(x, y); return new
     * ConstantVolatilitySurface(a[0] + a[1] * k + a[2] * k * k + a[3] * t +
     * a[4] * t * t + a[5] * k * t);
     */
    return null;
  }
}
