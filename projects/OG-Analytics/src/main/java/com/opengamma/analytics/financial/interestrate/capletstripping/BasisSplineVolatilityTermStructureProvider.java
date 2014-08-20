/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructure;
import com.opengamma.analytics.financial.model.volatility.VolatilityTermStructureProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BasisSplineVolatilityTermStructureProvider implements VolatilityTermStructureProvider<DoubleMatrix1D> {

  private final List<Function1D<Double, Double>> _bSplines;

  public BasisSplineVolatilityTermStructureProvider(final List<Function1D<Double, Double>> bSlines) {
    ArgumentChecker.noNulls(bSlines, "null bSplines");
    _bSplines = bSlines;
  }

  public BasisSplineVolatilityTermStructureProvider(final double t1, final double t2, final int nKnots, final int degree) {
    final BasisFunctionGenerator gen = new BasisFunctionGenerator();
    _bSplines = gen.generateSet(t1, t2, nKnots, degree);
  }

  @Override
  public VolatilityTermStructure evaluate(DoubleMatrix1D data) {
    final Function1D<Double, Double> func = new BasisFunctionAggregation<>(_bSplines, data.getData());
    return new VolatilityTermStructure() {

      @Override
      public Double getVolatility(Double t) {
        return func.evaluate(t);
      }
    };
  }

}
