/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.serialization.InnerClassSubstitution;

/**
 * 
 */
public class NelsonSiegelBondCurveModel {

  public ParameterizedFunction<Double, DoubleMatrix1D, Double> getParameterizedFunction() {
    return new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

      @Override
      public Double evaluate(final Double t, final DoubleMatrix1D parameters) {
        Validate.notNull(t, "t");
        Validate.notNull(parameters, "parameters");
        Validate.isTrue(parameters.getNumberOfElements() == 4);
        final double beta0 = parameters.getEntry(0);
        final double beta1 = parameters.getEntry(1);
        final double beta2 = parameters.getEntry(2);
        final double lambda = parameters.getEntry(3);
        final double x1 = t / lambda;
        final double x2 = (1 - Math.exp(-x1)) / x1;
        return beta0 + beta1 * x2 + beta2 * (x2 - Math.exp(-x1));
      }

      public Object writeReplace() {
        return new NelsonSiegelBondCurveModel.SerializedForm();
      }

    };
  }

  /**
   * Serialized form of the anonymous inner classes
   */
  public static final class SerializedForm {

    private SerializedForm() {
    }

  }

  // TODO: drop this fragment in with instrumentation
  /**
   * 
   */
  public static final InnerClassSubstitution s_serialization = new InnerClassSubstitution() {
    @Override
    protected Object invoke(Method method, Object object) throws Exception {
      return method.invoke(object);
    }
  };

}
