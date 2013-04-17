/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class VolTermStructureModelProvider extends VolatilityModelProvider {

  private final double[] _knots;
  private final Interpolator1D _interpolator;

  public VolTermStructureModelProvider(final double[] knotPoints, final Interpolator1D interpolator) {
    ArgumentChecker.notEmpty(knotPoints, "null or empty knotPoints");
    ArgumentChecker.notNull(interpolator, "null interpolator");
    _knots = knotPoints;
    _interpolator = interpolator;
  }

  public VolTermStructureModelProvider(final double[] knotPoints, final Interpolator1D baseInterpolator, final ParameterLimitsTransform parameterTransform) {
    ArgumentChecker.notEmpty(knotPoints, "null or empty knotPoints");
    ArgumentChecker.notNull(baseInterpolator, "null interpolator");
    ArgumentChecker.notNull(parameterTransform, "null parameterTransform");
    _knots = knotPoints;
    _interpolator = new TransformedInterpolator1D(baseInterpolator, parameterTransform);
  }

  @Override
  public VolatilityModel1D evaluate(DoubleMatrix1D x) {
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(_knots, x.getData(), _interpolator);
    return new VolatilityModel1D() {

      @Override
      public Double getVolatility(double[] fwdKT) {
        return curve.getYValue(fwdKT[2]);
      }

      @Override
      public double getVolatility(SimpleOptionData option) {
        return curve.getYValue(option.getTimeToExpiry());
      }

      @Override
      public double getVolatility(double forward, double strike, double timeToExpiry) {
        return curve.getYValue(timeToExpiry);
      }
    };
  }

}
