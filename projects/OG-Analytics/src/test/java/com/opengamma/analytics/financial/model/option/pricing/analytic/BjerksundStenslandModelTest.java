/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class BjerksundStenslandModelTest extends AmericanAnalyticOptionModelTest {

  private static final ProbabilityDistribution<double[]> BIVARIATE_NORMAL = new BivariateNormalDistribution();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Test
  public void test() {
    super.assertValid(new BjerksundStenslandModel(), 1e-4);
  }

  @Test
  public void biVarTest() {
    final double a = 0.45;
    final double b = -1.2;
    final double rho = 0.45;

    final double anal = NORMAL.getPDF(a) * NORMAL.getCDF((b - rho * a) / Math.sqrt(1 - rho * rho));
    final double eps = 1e-5;
    final double up = BIVARIATE_NORMAL.getCDF(new double[] {a + eps, b, rho });
    final double down = BIVARIATE_NORMAL.getCDF(new double[] {a - eps, b, rho });
    final double fd = (up - down) / 2 / eps;

    //System.out.println(expect + "\t" + fd);
    assertEquals(fd, anal, Math.abs(fd) * 2e-8);
  }

  @Test
  //(enabled = false)
  public void callTest() {
    final double[] s0Set = new double[] {90, 100, 110, 160 };
    final double k = 100;
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, 0.11 };
    final double sigma = 0.35;
    final double t = 0.5;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (double s0 : s0Set) {
      for (double b : bSet) {

        final double call = bs.getCallPrice(s0, k, sigma, t, r, b);
        final double[] sense = bs.getCallPriceAdjoint(s0, k, r, b, t, sigma);

        assertEquals("price " + s0 + " " + b, call, sense[0], 1e-13);
        //  System.out.println(call + "\t" + sense[0]);

        final double[] parms = new double[] {s0, k, r, b, t, sigma };
        final int n = parms.length;
        final double eps = 1e-5;

        for (int i = 0; i < n; i++) {
          double[] temp = Arrays.copyOf(parms, n);
          temp[i] += eps;
          double up = bs.getCallPrice(temp[0], temp[1], temp[5], temp[4], temp[2], temp[3]);
          //double up = bs.getCallPriceAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5])[0];
          temp[i] -= 2 * eps;
          double down = bs.getCallPrice(temp[0], temp[1], temp[5], temp[4], temp[2], temp[3]);
          //double down = bs.getCallPriceAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5])[0];
          double fd;
          if (i == 3 && Math.abs(b) < eps) {
            //there is a discontinuity in the gradient at at b == 0 r != 0, hence forward difference for the test
            fd = (up - sense[0]) / eps;
            assertEquals(i + " " + k + " " + b, fd, sense[i + 1], Math.abs(fd) * 1e-4);
          } else {
            fd = (up - down) / 2 / eps;
            assertEquals(i + " " + k + " " + b, fd, sense[i + 1], Math.abs(fd) * 1e-5);
          }
          // System.out.println(fd + "\t" + sense[i + 1]);

        }
      }
    }
  }

  @Test
  public void putTest() {
    final double[] s0Set = new double[] {60, 90, 100, 110 };
    final double k = 100;
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, 0.11 };
    final double sigma = 0.25;
    final double t = 1.5;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (double s0 : s0Set) {
      for (double b : bSet) {

        final double call = bs.getPutPrice(s0, k, sigma, t, r, b);
        final double[] sense = bs.getPutPriceAdjoint(s0, k, r, b, t, sigma);

        assertEquals("price " + s0 + " " + b, call, sense[0], 1e-13);
        //System.out.println(call + "\t" + sense[0]);

        final double[] parms = new double[] {s0, k, r, b, t, sigma };
        final int n = parms.length;
        final double eps = 1e-5;

        for (int i = 0; i < n; i++) {
          double[] temp = Arrays.copyOf(parms, n);
          temp[i] += eps;
          double up = bs.getPutPrice(temp[0], temp[1], temp[5], temp[4], temp[2], temp[3]);
          temp[i] -= 2 * eps;
          double down = bs.getPutPrice(temp[0], temp[1], temp[5], temp[4], temp[2], temp[3]);
          double fd;
          if (i == 3 && Math.abs(b) < eps) {
            //there is a discontinuity in the gradient at at b == 0 r != 0, hence backwards difference for the test
            fd = (sense[0] - down) / eps;
            assertEquals(i + " " + k + " " + b, fd, sense[i + 1], Math.abs(fd) * 1e-4);
          } else {
            fd = (up - down) / 2 / eps;
            assertEquals(i + " " + k + " " + b, fd, sense[i + 1], Math.abs(fd) * 1e-5);
          }

        }
      }
    }
  }

  @Test
  public void phiTest() {
    final double s0 = 100;
    final double[] kSet = new double[] {90, 100, 110 };
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, r };
    final double t1 = 0.5;
    final double sigma = 0.35;

    final double t = 2 * t1 / (Math.sqrt(5) - 1);
    final double[] gammaSet = new double[] {0, 1, 0.67, 1.87 };
    final double x1 = 133.0;
    final double x2 = 140.2;

    for (double k : kSet) {
      for (double b : bSet) {
        for (double gamma : gammaSet) {

          final double[] parms = new double[] {s0, t, gamma, x1, x2, r, b, sigma };
          final int n = parms.length;

          BjerksundStenslandModel bs = new BjerksundStenslandModel();
          final double impA = bs.getPhi(s0, t1, gamma, x1, x2, r, b, sigma);
          final double[] sense = bs.getPhiAdjoint(s0, t, gamma, x1, x2, r, b, sigma);

          // System.out.println(impA + "\t" + sense[0]);
          assertEquals(impA, sense[0], 1e-12);
          final double eps = 1e-5;
          for (int i = 0; i < 8; i++) {
            double[] temp = Arrays.copyOf(parms, n);
            final double delta = (1 + Math.abs(parms[i])) * eps;
            temp[i] += delta;
            double up = bs.getPhiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7])[0];
            temp[i] -= 2 * delta;
            double down = bs.getPhiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7])[0];
            double fd = (up - down) / 2 / delta;
            //System.out.println(fd + "\t" + sense[i + 1]);
            assertEquals(i + " " + k + " " + b + " " + gamma, fd, sense[i + 1], Math.abs(fd) * 1e-8);
          }
        }
      }
    }

  }

  @Test
  public void psiTest() {
    final double s0 = 100;
    final double[] kSet = new double[] {90, 100, 110 };
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.04 };
    final double t = 0.5;
    final double sigma = 0.35;

    final double r2 = 0.5 * (Math.sqrt(5) - 1);
    final double t1 = r2 * t;
    final double[] gammaSet = new double[] {0, 1, 0.67, 1.87 };
    final double x1 = 133.0;
    final double x2 = 140.2;

    for (double k : kSet) {
      for (double b : bSet) {
        for (double gamma : gammaSet) {

          final double[] parms = new double[] {s0, t, gamma, k, x2, x1, r, b, sigma };
          final int n = parms.length;

          BjerksundStenslandModel bs = new BjerksundStenslandModel();
          final double impA = bs.getPsi(s0, t1, t, gamma, k, x2, x1, r, b, sigma);
          final double[] sense = bs.getPsiAdjoint(s0, t, gamma, k, x2, x1, r, b, sigma);

          assertEquals(impA, sense[0], 1e-12);
          //System.out.println(impA + "\t" + sense[0]);

          final double eps = 1e-5;
          for (int i = 0; i < n; i++) {
            double[] temp = Arrays.copyOf(parms, n);
            final double delta = (1 + Math.abs(parms[i])) * eps;
            temp[i] += delta;
            double up = bs.getPsiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7], temp[8])[0];
            temp[i] -= 2 * delta;
            double down = bs.getPsiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7], temp[8])[0];
            double fd = (up - down) / 2 / delta;
            //           System.out.println(fd + "\t" + sense[i + 1]);

            assertEquals(i + " " + k + " " + b + " " + gamma, fd, sense[i + 1], Math.abs(fd) * 1e-4); //TODO would expect better agreement than this 

          }
        }
      }
    }
  }

  @Test
  public void alphaTest() {
    final double k = 123;
    final double x = 204;
    final double beta = 2.3;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    final double[] sense = bs.getAlphaAdjoint(k, x, beta);
    final double[] parms = new double[] {k, x, beta };
    final int n = parms.length;
    final double eps = 1e-5;
    for (int i = 0; i < n; i++) {
      double[] temp = Arrays.copyOf(parms, n);
      temp[i] += eps;
      double up = bs.getAlphaAdjoint(temp[0], temp[1], temp[2])[0];
      temp[i] -= 2 * eps;
      double down = bs.getAlphaAdjoint(temp[0], temp[1], temp[2])[0];
      double fd = (up - down) / 2 / eps;
      // System.out.println(fd + "\t" + sense[i + 1]);
      assertEquals(fd, sense[i + 1], Math.abs(fd) * 1e-8);
    }
  }

  @Test
  public void betaTest() {
    final double r = 0.1;
    final double b = 0.04;
    final double sigma = 0.3;
    final double sigmaSq = sigma * sigma;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    final double[] sense = bs.getBetaAdjoint(r, b, sigmaSq);
    final double[] parms = new double[] {r, b, sigmaSq };
    final int n = parms.length;
    final double eps = 1e-5;
    for (int i = 0; i < n; i++) {
      double[] temp = Arrays.copyOf(parms, n);
      temp[i] += eps;
      double up = bs.getBetaAdjoint(temp[0], temp[1], temp[2])[0];
      temp[i] -= 2 * eps;
      double down = bs.getBetaAdjoint(temp[0], temp[1], temp[2])[0];
      double fd = (up - down) / 2 / eps;
      //  System.out.println(fd + "\t" + sense[i + 1]);
      assertEquals(fd, sense[i + 1], Math.abs(fd) * 1e-8);
    }
  }

  @Test
  public void lambdaTest() {
    final double[] gammaSet = new double[] {0, 1, 0.9, 2.3 };
    final double r = 0.1;
    final double[] bSet = {-0.03, 0, 0.04, r };
    final double sigma = 0.3;
    final double sigmaSq = sigma * sigma;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (double gamma : gammaSet) {
      for (double b : bSet) {

        final double[] sense = bs.getLambdaAdjoint(gamma, r, b, sigmaSq);
        final double[] parms = new double[] {gamma, r, b, sigmaSq };
        final int n = parms.length;
        final double eps = 1e-5;
        for (int i = 0; i < n; i++) {
          double[] temp = Arrays.copyOf(parms, n);
          temp[i] += eps;
          double up = bs.getLambdaAdjoint(temp[0], temp[1], temp[2], temp[3])[0];
          temp[i] -= 2 * eps;
          double down = bs.getLambdaAdjoint(temp[0], temp[1], temp[2], temp[3])[0];
          double fd = (up - down) / 2 / eps;
          //   System.out.println(fd + "\t" + sense[i + 1]);
          assertEquals(fd, sense[i + 1], Math.abs(fd) * 1e-8);
        }
      }
    }
  }

  @Test
  public void kappaTest() {
    final double[] gammaSet = new double[] {0, 1, 0.9, 2.3 };
    final double[] bSet = {-0.03, 0, 0.04 };
    final double sigma = 0.3;
    final double sigmaSq = sigma * sigma;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (double gamma : gammaSet) {
      for (double b : bSet) {

        final double[] sense = bs.getKappaAdjoint(gamma, b, sigmaSq);
        final double[] parms = new double[] {gamma, b, sigmaSq };
        final int n = parms.length;
        final double eps = 1e-5;
        for (int i = 0; i < n; i++) {
          double[] temp = Arrays.copyOf(parms, n);
          temp[i] += eps;
          double up = bs.getKappaAdjoint(temp[0], temp[1], temp[2])[0];
          temp[i] -= 2 * eps;
          double down = bs.getKappaAdjoint(temp[0], temp[1], temp[2])[0];
          double fd = (up - down) / 2 / eps;
          //System.out.println(fd + "\t" + sense[i + 1]);
          assertEquals(fd, sense[i + 1], Math.abs(fd) * 2e-8);
        }
      }
    }
  }

  @Test
  public void i1Test() {
    final double k = 100.04;
    final double r = 0.1;
    final double[] bb = new double[] {-0.03, 0.04 };
    final double sigma = 0.3;
    final double t = 0.3;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (double b : bb) {

      final double[] sense = bs.getI1Adjoint(k, r, b, sigma, t);
      final double[] parms = new double[] {k, r, b, sigma, t };
      final int n = parms.length;
      final double eps = 1e-5;
      for (int i = 0; i < n; i++) {
        double[] temp = Arrays.copyOf(parms, n);
        temp[i] += eps;
        double up = bs.getI1Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
        temp[i] -= 2 * eps;
        double down = bs.getI1Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
        double fd = (up - down) / 2 / eps;
        //System.out.println(fd + "\t" + sense[i + 1]);
        assertEquals(fd, sense[i + 1], Math.abs(fd) * 1e-7);
      }
    }
  }

  @Test
  public void i2Test() {
    final double k = 100.04;
    final double r = 0.1;
    final double[] bb = new double[] {-0.03, 0.04 };
    final double sigma = 0.3;
    final double t = 1.3;

    BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (double b : bb) {

      final double[] sense = bs.getI2Adjoint(k, r, b, sigma, t);
      final double[] parms = new double[] {k, r, b, sigma, t };
      final int n = parms.length;
      final double eps = 1e-5;
      for (int i = 0; i < n; i++) {
        double[] temp = Arrays.copyOf(parms, n);
        temp[i] += eps;
        double up = bs.getI2Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
        temp[i] -= 2 * eps;
        double down = bs.getI2Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
        double fd = (up - down) / 2 / eps;
        //   System.out.println(fd + "\t" + sense[i + 1]);
        assertEquals(fd, sense[i + 1], Math.abs(fd) * 1e-7);
      }
    }
  }

}
