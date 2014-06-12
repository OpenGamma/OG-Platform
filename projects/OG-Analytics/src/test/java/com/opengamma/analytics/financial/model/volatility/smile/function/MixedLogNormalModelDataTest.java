/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MixedLogNormalModelDataTest {

  @Test
  public void constructionTest() {
    double[] w = new double[] {0.7, 0.1, 0.2 };
    double[] sigma = new double[] {0.2, 0.5, 1.0 };
    MixedLogNormalModelData data = new MixedLogNormalModelData(w, sigma);
    int n = w.length;
    double[] w2 = data.getWeights();
    double[] sigma2 = data.getVolatilities();
    double[] f = data.getRelativeForwards();
    for (int i = 0; i < n; i++) {
      assertEquals(w[i], w2[i], 1e-15, "wrong weights");
      assertEquals(sigma[i], sigma2[i], 1e-15, "wrong vols");
      assertEquals(1.0, f[i], 1e-15, "wrong forwards");
    }
  }

  @Test
  public void constructionTest2() {
    double[] w = new double[] {0.1, 0.1, 0.75, 0.05 };
    double[] sigma = new double[] {0.05, 0.05, 0.2, 1.0 };
    double[] f = new double[] {0.8, 1.3, 1.0, Double.NaN };
    int n = w.length;
    double temp = 0;
    for (int i = 0; i < n - 1; i++) {
      temp += w[i] * f[i];
    }
    f[n - 1] = (1.0 - temp) / w[n - 1];
    ArgumentChecker.isTrue(f[n - 1] > 0.0, "Adjust test parameters - f[{}] = {}", n - 1, f[n - 1]);
    MixedLogNormalModelData data = new MixedLogNormalModelData(w, sigma, f);

    double[] w2 = data.getWeights();
    double[] sigma2 = data.getVolatilities();
    double[] f2 = data.getRelativeForwards();
    for (int i = 0; i < n; i++) {
      assertEquals(w[i], w2[i], 1e-15, "wrong weights");
      assertEquals(sigma[i], sigma2[i], 1e-15, "wrong vols");
      assertEquals(f[i], f2[i], 1e-15, "wrong forwards");
    }
  }

  @Test
  public void constructionTest3() {
    final int n = 3;
    final int np = 3 * n - 2;
    double[] p = new double[np];
    p[0] = 0.2;
    for (int i = 1; i < n; i++) {
      p[i] = 0.5 * Math.random();
    }
    for (int i = n; i < np; i++) {
      p[i] = 2 * Math.PI * Math.random();
    }

    MixedLogNormalModelData data = new MixedLogNormalModelData(p);

    assertEquals(np, data.getNumberOfParameters());
    for (int i = 0; i < n; i++) {
      assertEquals(p[i], data.getParameter(i), 1e-15);
    }

    double[] w2 = data.getWeights();
    double[] sigma2 = data.getVolatilities();
    double[] f2 = data.getRelativeForwards();
    double sumW = 0.0;
    double sumWF = 0.0;
    assertEquals(p[0], sigma2[0], 1e-15, "wrong vols");
    for (int i = 0; i < n; i++) {
      if (i > 0) {
        assertEquals(p[i], sigma2[i] - sigma2[i - 1], 1e-15, "wrong vols");
      }
      sumW += w2[i];
      sumWF += w2[i] * f2[i];
    }
    assertEquals(1.0, sumW, 1e-15, "wrong weights");
    assertEquals(1.0, sumWF, 1e-15, "wrong forwards");
  }

  @Test
  public void constructionTest4() {
    final int n = 5;
    final int np = 2 * n - 1;
    double[] p = new double[np];
    p[0] = 0.2;
    for (int i = 1; i < n; i++) {
      p[i] = 0.5 * Math.random();
    }
    for (int i = n; i < np; i++) {
      p[i] = 2 * Math.PI * Math.random();
    }

    MixedLogNormalModelData data = new MixedLogNormalModelData(p, false);

    assertEquals(np, data.getNumberOfParameters());
    for (int i = 0; i < n; i++) {
      assertEquals(p[i], data.getParameter(i), 1e-15);
    }

    double[] w2 = data.getWeights();
    double[] sigma2 = data.getVolatilities();
    double[] f2 = data.getRelativeForwards();
    double sumW = 0.0;

    assertEquals(p[0], sigma2[0], 1e-15, "wrong vols");
    for (int i = 0; i < n; i++) {
      if (i > 0) {
        assertEquals(p[i], sigma2[i] - sigma2[i - 1], 1e-15, "wrong vols");
      }
      sumW += w2[i];
      assertEquals(1.0, f2[i], 1e-15, "wrong forwards");
    }
    assertEquals(1.0, sumW, 1e-15, "wrong weights");
  }

}
