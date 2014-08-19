/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.function.VectorFunction;
import com.opengamma.analytics.math.function.VectorFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class DiscreteVolatilityFunctionProviderFromInterpolatedTermStructureTest {

  private static final Interpolator1D INTERPOLATOR;
  private static final double[] KNOTS = new double[] {0.25, 0.5, 1.0, 3.0, 5.0, 7.0, 10.0 };

  static {
    INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  }

  @Test
  public void test() {
    final DiscreteVolatilityFunctionProvider discPro = new DiscreteVolatilityFunctionProviderFromInterpolatedTermStructure(KNOTS, INTERPOLATOR);

    //chosen some 'random' expiry-strike points - recall the surface does not depend on the strike 
    final List<DoublesPair> points = new ArrayList<>();
    points.add(DoublesPair.of(3.4, 0.05));
    points.add(DoublesPair.of(0.1, 0.06));
    points.add(DoublesPair.of(12.0, 0.05));
    points.add(DoublesPair.of(5.0, 0.12));
    final DiscreteVolatilityFunction func = discPro.from(points);

    assertEquals(KNOTS.length, func.getLengthOfDomain());
    assertEquals(points.size(), func.getLengthOfRange());

    final DoubleMatrix1D knotValues = new DoubleMatrix1D(0.7, 0.6, 0.55, 0.7, 1.2, 1.0, 0.8);
    final DoubleMatrix1D vols1 = func.evaluate(knotValues);

    //InterpolatedVectorFunctionProvider is tested separately, so we can use it here as a benchmark 
    final VectorFunctionProvider<Double> vfp = new InterpolatedVectorFunctionProvider(INTERPOLATOR, KNOTS);
    final VectorFunction vf = vfp.from(new Double[] {3.4, 0.1, 12.0, 5.0 });
    final DoubleMatrix1D vols2 = vf.evaluate(knotValues);
    AssertMatrix.assertEqualsVectors(vols2, vols1, 1e-13);

    final DoubleMatrix2D jac1 = func.calculateJacobian(knotValues);
    final DoubleMatrix2D jac2 = vf.calculateJacobian(knotValues);
    AssertMatrix.assertEqualsMatrix(jac2, jac1, 1e-14);
  }

}
