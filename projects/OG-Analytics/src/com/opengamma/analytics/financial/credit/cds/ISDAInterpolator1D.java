/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Curve interpolation as defined by ISDA
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class ISDAInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(Interpolator1DDataBundle data, Double value) {
    
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");
    
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    
    // Avoid divide by zero errors (offset factors out of the final result if it is used)
    final double offset = value == 0.0 ? 1.0 : 0.0;
    
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    final double y1x1 = y1 * (x1 + offset);
    
    if (data.getLowerBoundIndex(value) == data.size() - 1) {
      return y1;
    }
    
    final double x2 = boundedValues.getHigherBoundKey();
    final double y2 = boundedValues.getHigherBoundValue();
    final double y2x2 = y2 * (x2 + offset);
    
    return (y1x1 + (value - x1) / (x2 - x1) * (y2x2 - y1x1)) / (value + offset);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    throw new UnsupportedOperationException("Nodal sensitivities are not supported for the ISDA interpolation method");
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(double[] x, double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(double[] x, double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

}
