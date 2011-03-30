/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.NullTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.TransformParameters;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;

/**
 * 
 */
public class NelsonSiegelSvennsonBondCurveModel extends Function1D<Double, Double> {

  // TODO: Note that if you rewrite this to match the structure of things like NelsonSiegelBondCurveModel, the builder will be wrong

  private static final ParameterLimitsTransform[] TRANSFORMS = new ParameterLimitsTransform[] {new SingleRangeLimitTransform(0, LimitType.GREATER_THAN), new NullTransform(), new NullTransform(),
    new NullTransform(), new NullTransform(), new NullTransform()};
  private static final BitSet FIXED_PARAMETERS = new BitSet(6);
  private final double _beta0;
  private final double _beta1;
  private final double _beta2;
  private final double _lambda1;
  private final double _beta3;
  private final double _lambda2;
  private final TransformParameters _transform;
  private final DoubleMatrix1D _parameters;

  public NelsonSiegelSvennsonBondCurveModel(final DoubleMatrix1D parameters) {
    Validate.notNull(parameters, "parameters");
    Validate.isTrue(parameters.getNumberOfElements() == 6);
    _beta0 = parameters.getEntry(0);
    _beta1 = parameters.getEntry(1);
    _beta2 = parameters.getEntry(2);
    _lambda1 = parameters.getEntry(3);
    _beta3 = parameters.getEntry(4);
    _lambda2 = parameters.getEntry(5);
    _parameters = parameters;
    _transform = new TransformParameters(parameters, TRANSFORMS, FIXED_PARAMETERS); // TODO no no no no no
  }

  public DoubleMatrix1D getParameters() {
    return _parameters;
  }

  public TransformParameters getTransform() {
    return _transform;
  }

  @Override
  public Double evaluate(final Double t) {
    Validate.notNull(t, "t");
    final double x1 = t / _lambda1;
    final double x2 = (1 - Math.exp(-x1)) / x1;
    final double x3 = t / _lambda2;
    final double x4 = (1 - Math.exp(-x3)) / x3;
    return _beta0 + _beta1 * x2 + _beta2 * (x2 - Math.exp(-x1)) + _beta3 * (x4 - Math.exp(-x3));
  }

  public ParameterizedFunction<Double, DoubleMatrix1D, Double> getParameterizedFunction() {
    return new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

      @Override
      public Double evaluate(final Double t, final DoubleMatrix1D transformedParameters) {
        Validate.notNull(transformedParameters, "parameters");
        @SuppressWarnings("synthetic-access")
        final DoubleMatrix1D modelParameters = _transform.inverseTransform(transformedParameters);
        return new NelsonSiegelSvennsonBondCurveModel(modelParameters).evaluate(t);
      }

      @Override
      public final Function1D<Double, Double> asFunctionOfArguments(final DoubleMatrix1D params) {
        return new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double t) {
            return new NelsonSiegelSvennsonBondCurveModel(params).evaluate(t);
          }
        };
      }

    };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_beta0);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_beta1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_beta2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_beta3);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambda1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambda2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NelsonSiegelSvennsonBondCurveModel other = (NelsonSiegelSvennsonBondCurveModel) obj;
    if (Double.doubleToLongBits(_beta0) != Double.doubleToLongBits(other._beta0)) {
      return false;
    }
    if (Double.doubleToLongBits(_beta1) != Double.doubleToLongBits(other._beta1)) {
      return false;
    }
    if (Double.doubleToLongBits(_beta2) != Double.doubleToLongBits(other._beta2)) {
      return false;
    }
    if (Double.doubleToLongBits(_beta3) != Double.doubleToLongBits(other._beta3)) {
      return false;
    }
    if (Double.doubleToLongBits(_lambda1) != Double.doubleToLongBits(other._lambda1)) {
      return false;
    }
    return Double.doubleToLongBits(_lambda2) == Double.doubleToLongBits(other._lambda2);
  }
}
