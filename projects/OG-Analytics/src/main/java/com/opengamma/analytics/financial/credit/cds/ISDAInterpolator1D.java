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
 * Curve interpolation as defined by ISDA. This is nothing more that linear interpolation of the quantity x*y, which is  
 * the (negative) log of the survival rate.
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @deprecated Use classes from isdastandardmodel
 */
@Deprecated
public class ISDAInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {

    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");
    try {
      final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);

      // Avoid divide by zero errors (offset factors out of the final result if it is used)
      //R White where does this come from? The economically relevant quantity is x*y which is the (negative) log of the survival rate,
      //which is 0 for x (i.e. time t) equal 0. What this does is return y(1) for x = 0.0, which is arbitrary.  
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
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {

    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Data bundle must not be null");
    try {
      final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);

      final double offset = value == 0.0 ? 1.0 : 0.0;

      final double x1 = boundedValues.getLowerBoundKey();
      final double y1 = boundedValues.getLowerBoundValue();
      final double y1x1 = y1 * (x1 + offset);

      if (data.getLowerBoundIndex(value) == data.size() - 1) {
        return 0.;
      }

      final double x2 = boundedValues.getHigherBoundKey();
      final double y2 = boundedValues.getHigherBoundValue();
      final double y2x2 = y2 * (x2 + offset);

      final double valueWithOffset = value + offset;
      return (y2x2 - y1x1) / (x2 - x1) / valueWithOffset - (y1x1 + (value - x1) / (x2 - x1) * (y2x2 - y1x1)) / valueWithOffset / valueWithOffset;
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    throw new UnsupportedOperationException("Nodal sensitivities are not supported for the ISDA interpolation method");
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

}
