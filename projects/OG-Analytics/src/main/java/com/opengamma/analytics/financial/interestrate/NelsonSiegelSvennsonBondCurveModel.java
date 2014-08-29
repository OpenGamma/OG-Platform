/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.ParameterizedFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;

/**
 * 
 */
public class NelsonSiegelSvennsonBondCurveModel {

  public ParameterizedFunction<Double, DoubleMatrix1D, Double> getParameterizedFunction() {
    return new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

      @Override
      public Double evaluate(final Double t, final DoubleMatrix1D parameters) {
        Validate.notNull(t, "t");
        Validate.notNull(parameters, "parameters");
        Validate.isTrue(parameters.getNumberOfElements() == getNumberOfParameters());
        final double beta0 = parameters.getEntry(0);
        final double beta1 = parameters.getEntry(1);
        final double beta2 = parameters.getEntry(2);
        final double lambda1 = parameters.getEntry(3);
        final double beta3 = parameters.getEntry(4);
        final double lambda2 = parameters.getEntry(5);
        final double x1 = t / lambda1;
        final double x2 = (1 - Math.exp(-x1)) / x1;
        final double x3 = t / lambda2;
        final double x4 = (1 - Math.exp(-x3)) / x3;
        return beta0 + beta1 * x2 + beta2 * (x2 - Math.exp(-x1)) + beta3 * (x4 - Math.exp(-x3));
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(NelsonSiegelSvennsonBondCurveModel.class, "getParameterizedFunction");
      }

      @Override
      public int getNumberOfParameters() {
        return 6;
      }

    };
  }

}
