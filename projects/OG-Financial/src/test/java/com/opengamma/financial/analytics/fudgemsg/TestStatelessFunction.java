/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class TestStatelessFunction {

  public Function<Double, Double> getSurface(final Double a, final Double b) {
    final Function<Double, Double> f = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return a * x[0] + b * x[1] * x[1];
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(TestStatelessFunction.class, "getSurface", a, b);
      }
    };
    return f;
  }
}
