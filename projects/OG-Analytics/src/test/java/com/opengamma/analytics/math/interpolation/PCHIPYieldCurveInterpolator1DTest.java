/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PCHIPYieldCurveInterpolator1DTest {
  private static final Interpolator1D INTERPOLATOR = Interpolator1DFactory.getInterpolator(Interpolator1DFactory.MOD_PCHIP);

  private static final double[] T = new double[] {0, 0.5150684931506849, 0.7671232876712328, 1.010958904109589, 2.010958904109589, 3.0181600419193053, 4.013698630136986, 5.010958904109589,
      6.010958904109589, 7.012695561044988, 8.01917808219178, 9.016438356164384, 10.013698630136986, 12.01095890410959, 15.015427801482147, 20.016438356164382, 30.01095890410959};
  private static final double[] R = new double[] {0, 0.0030710272047332716, 0.003040656562873157, 0.003110123377945795, 0.003953252035721237, 0.00509347879741538, 0.006590689152566608,
      0.008332110602980983, 0.010112519240233082, 0.011847052514052383, 0.013507980810870852, 0.015055988140506857, 0.016458025496989874, 0.018806316169650072, 0.021315358640481088,
      0.0231810574586673, 0.023940706883886057};

  private static final double[] TR;

  private static final Interpolator1DDataBundle DATA = INTERPOLATOR.getDataBundle(T, R);

  static {
    final int n = T.length;
    TR = new double[n];
    for (int i = 0; i < n; i++) {
      TR[i] = T[i] * R[i];
    }
  }

  @Test(enabled=false)
  public void printTest() {
    System.out.println("PCHIPYieldCurveInterpolator1DTest");
    final int nSamples = 201;
    for (int i = 0; i < nSamples; i++) {
      final double t = 30.0 * i / ((nSamples - 1));
      final double r = INTERPOLATOR.interpolate(DATA, t);
      System.out.println(t + "\t" + r);
    }
  }

  @Test
  public void test() {
    final int n = T.length;
    for(int i=1;i<n;i++) { //cannot recover the t=0 value
      final double r = INTERPOLATOR.interpolate(DATA, T[i]);
      assertEquals(R[i],r,1e-15);
    }
  }



}
