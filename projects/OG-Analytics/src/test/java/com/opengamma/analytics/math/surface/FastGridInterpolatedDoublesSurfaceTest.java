/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class FastGridInterpolatedDoublesSurfaceTest {
  private static final Interpolator1D X_INTERPOLATOR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final Interpolator1D Y_INTERPOLATOR = Interpolator1DFactory.LOG_LINEAR_INSTANCE;
  private static final double[] X_VALUES =
    {0., 0., 0., 0.,
     1., 1., 1., 1.,
     2., 2., 2., 2.,
     3., 3., 3., 3.};
  private static final double[] Y_VALUES =
    {14., 10., 28., 20.,
     13., 11., 24., 21.,
     10., 12., 26, 22.,
     11., 13., 22., 23.};
  private static final double[] Z_VALUES =
    {4., 5., 6., 7.,
     4., 5., 6., 7.,
     8., 9., 10., 11.,
     8., 9., 10., 11.};
  private static final long SEED_FOR_FUZZER = 11760811L;
  private static final int FUZZER_COUNT = 500;
  
  @Test
  public void testFuzzingAgainstInterpolatedDoublesSurface() {
    FastGridInterpolatedDoublesSurface fast = FastGridInterpolatedDoublesSurface.fromUnsortedNoCopy(X_VALUES, Y_VALUES, Z_VALUES, X_INTERPOLATOR, Y_INTERPOLATOR, "Fuzzy");
    InterpolatedDoublesSurface control = new InterpolatedDoublesSurface(X_VALUES, Y_VALUES, Z_VALUES, new GridInterpolator2D(X_INTERPOLATOR, Y_INTERPOLATOR));
    
    Random random = new Random(SEED_FOR_FUZZER);
    for (int i = 0; i < FUZZER_COUNT; i++) {
      double x = random.nextDouble() * 3;
      double y = (random.nextDouble() * 12) + 11;
      
      Double controlZ = control.getZValue(x, y);
      Double testZ = fast.getZValue(x, y);
      Assert.assertEquals(controlZ, testZ, 0.005);
    }
  }
}
