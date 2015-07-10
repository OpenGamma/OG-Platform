/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Bjerksund and Stensland model test.
 */
@Test(groups = TestGroup.UNIT)
public class BjerksundStenslandModelTest {
  
  private static  final BjerksundStenslandModel model = new BjerksundStenslandModel();
  
  private static final ScalarFieldFirstOrderDifferentiator SCALAR_FIELD_DIFF = new ScalarFieldFirstOrderDifferentiator();
  private static final VectorFieldFirstOrderDifferentiator VEC_FIELD_DIFF = new VectorFieldFirstOrderDifferentiator();
  
  private static final ProbabilityDistribution<double[]> BIVARIATE_NORMAL = new BivariateNormalDistribution();

  private static final Function1D<DoubleMatrix1D, Double> CALL_PRICE_FUNC = new Function1D<DoubleMatrix1D, Double>() {
    @Override
    public Double evaluate(DoubleMatrix1D parms) {
      double[] p = parms.getData();
      return model.price(p[0], p[1], p[2], p[3], p[4], p[5], true);
    }
  };
  
  private static final Function1D<DoubleMatrix1D, Double> PUT_PRICE_FUNC = new Function1D<DoubleMatrix1D, Double>() {
    @Override
    public Double evaluate(DoubleMatrix1D parms) {
      double[] p = parms.getData();
      return model.price(p[0], p[1], p[2], p[3], p[4], p[5], false);
    }
  };
  
  private static final Function1D<DoubleMatrix1D,DoubleMatrix1D> CALL_SENSE_FUNC = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D parms) {
      double[] p = parms.getData();
      double[] adj =  model.getPriceAdjoint(p[0], p[1], p[2], p[3], p[4], p[5], true);
      double[] temp = new double[6];
      System.arraycopy(adj, 1, temp, 0, 6);
      return new DoubleMatrix1D(temp);
    }
  };
  
  private static final Function1D<DoubleMatrix1D,DoubleMatrix1D> PUT_SENSE_FUNC = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D parms) {
      double[] p = parms.getData();
      double[] adj =  model.getPriceAdjoint(p[0], p[1], p[2], p[3], p[4], p[5], false);
      double[] temp = new double[6];
      System.arraycopy(adj, 1, temp, 0, 6);
      return new DoubleMatrix1D(temp);
    }
  };

  private static final Function1D<DoubleMatrix1D, Boolean> DOMAIN = new Function1D<DoubleMatrix1D, Boolean>() {
    @Override
    public Boolean evaluate(DoubleMatrix1D parms) {
      double[] x = parms.getData();
      return x[0] >= 0.0 && x[1] >= 0.0 && x[4] >= 0.0 && x[5] >= 0.0;
    }
  };

  

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> CALL_ADJOINT_FD = SCALAR_FIELD_DIFF.differentiate(CALL_PRICE_FUNC, DOMAIN);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> PUT_ADJOINT_FD = SCALAR_FIELD_DIFF.differentiate(PUT_PRICE_FUNC, DOMAIN);

  @Test
  public void priceTest() {
    final double s0 = 120;
    final double r = 0.08;
    final double q = 0.12;
    final double b = r - q;
    final double k = 100.0;
    final double t = 0.25;
    final double sigma = 0.3;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double eurPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, true);
    final double amPrice = bs.price(s0, k, r, b, t, sigma, true);
    //System.out.println(eurPrice + "\t" + amPrice);
    assertTrue(amPrice > eurPrice);
    assertEquals(20.193913138412203, amPrice, 1e-15);
  }

  @Test
  //(enabled = false)
  public void priceAdjointBsRecapTest() {
    final double s0 = 120;
    final double r = -0.12;
    final double[] costs = new double[] {-0.12, 0.12 };
    final double k = 100.0;
    final double t = 0.25;
    final double[] sigmas = new double[] {0.0, 0.2, 0.5 };

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    for (final double sigma : sigmas) {
      for (final double b : costs) {
        final double resPrice = bs.price(s0, k, r, b, t, sigma, false);
        final double[] resGamma = bs.getPriceDeltaGamma(s0, k, r, b, t, sigma, false);
        assertEquals(b + "\t" + sigma, resPrice, resGamma[0], 1e-13);
      }
    }
  }

  /**
   * Check the sensitivity calculated by getPriceAdjoint and getPriceDeltaGamma agree with the finite-difference 
   * calculation
   */
  @Test
  public void priceAdjointBsTest() {
    final double s0 = 120;
    final double r = -0.12;
    final double[] costs = new double[] {-0.12, 0.12 };
    final double k = 100.0;
    final double t = 0.25;
    final double[] sigmas = new double[] {0.0, 0.2, 0.25, 0.3 };

    Function1D<DoubleMatrix1D, DoubleMatrix2D> fd2OrderFunc = VEC_FIELD_DIFF.differentiate(PUT_SENSE_FUNC, DOMAIN);
    for (final double sigma : sigmas) {
      for (final double b : costs) {
        DoubleMatrix1D pVec = new DoubleMatrix1D(s0, k, r, b, t, sigma );
        double price = PUT_PRICE_FUNC.evaluate(pVec);       
        double[] res = model.getPriceAdjoint(s0, k, r, b, t, sigma, false);  
        assertEquals(b + "\t" + sigma, price, res[0], Math.abs(price) * 1e-14);
        double[] temp = new double[6];
        System.arraycopy(res, 1, temp, 0, 6);
        DoubleMatrix1D priceAdj = new DoubleMatrix1D(temp);
        
        //compute adjoint by FD
        DoubleMatrix1D fdPriceAdj = PUT_ADJOINT_FD.evaluate(pVec);
        AssertMatrix.assertEqualsVectors(fdPriceAdj, priceAdj, 1e-8);

        //check the this produces the correct price and delta, and check the gamma against fd (of the delta)
        final double[] resGamma = model.getPriceDeltaGamma(s0, k, r, b, t, sigma, false);
        assertEquals(b + "\t" + sigma, price, resGamma[0], Math.max(1.e-14, Math.abs(price) * 1e-14));
        assertEquals(res[1], resGamma[1], Math.max(1.e-14, Math.abs(res[1]) * 1e-14));
              
        double fd_gamma = fd2OrderFunc.evaluate(pVec).getEntry(0,0);
        assertEquals(fd_gamma, resGamma[2], Math.abs(fd_gamma) * 1e-8);
     }
    }
  }

  /**
   * Discontinuity of derivative value exists at b=r for call, where the switch from Bjerksund-Stensland model to Black model takes place.
   * In our implementation we use the derivative values of the Black model at the transition point
   */
  @Test
  public void priceAdjointDiscontTest() {
    final double s0 = 120;
    final double r = -0.12;
    final double[] costs = new double[] {-0.12 };
    final double k = 100.0;
    final double t = 0.25;
    final double[] sigmas = new double[] { 0.3 };

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    for (final double sigma : sigmas) {
      for (final double b : costs) {
        final double[] params = new double[] {s0, k, r, b, t, sigma };
        final int nParams = params.length;
        final double[] res = bs.getPriceAdjoint(s0, k, r, b, t, sigma, true);
        final double resPrice = bs.price(s0, k, r, b, t, sigma, true);
        assertEquals(resPrice, res[0], Math.abs(resPrice) * 1e-14);

        for (int i = 0; i < nParams; ++i) {
          final double[] up = new double[nParams];
          final double[] down = new double[nParams];
          System.arraycopy(params, 0, up, 0, nParams);
          System.arraycopy(params, 0, down, 0, nParams);
          up[i] *= (1. + 1.e-4);
          down[i] *= (1. - 1.e-4);
          final double upRes = bs.price(up[0], up[1], up[2], up[3], up[4], up[5], true);
          final double downRes = bs.price(down[0], down[1], down[2], down[3], down[4], down[5], true);
          double fin = 0.;
          if (i == 2) {
            fin = (upRes - resPrice) / params[i] / 1.e-4;
          } else {
            if (i == 3) {
              fin = (resPrice - downRes) / params[i] / 1.e-4;

            } else {
              fin = 0.5 * (upRes - downRes) / params[i] / 1.e-4;
            }
          }

          assertEquals(i + "", fin, res[i + 1], Math.abs(params[i]) * 1e-3);
        }
      }
    }
  }

  // [PLAT-2944]
  @Test
  public void earlyExciseTest() {
    final double s0 = 10.0;
    final double r = 0.0;
    final double b = 0.05;
    final double k = 13.0;
    final double t = 0.25;
    final double sigma = 0.3;
    final boolean isCall = false;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double amPrice = bs.price(s0, k, r, b, t, sigma, isCall);
    assertTrue(amPrice >= (k - s0));
  }

  @Test
  public void adjointTest() {
    final double[] s0Set = new double[] {60, 90, 100, 110, 160 };
    final double k = 100;
    final double[] rSet = new double[] {0.0, 0.1 };
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, 0.11 };
    final double sigma = 0.35;
    final double t = 0.5;
    final boolean[] tfSet = new boolean[] {true, false };

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double eps = 1e-5;

    for (final double s0 : s0Set) {
      for (final double r : rSet) {
        for (final double b : bSet) {
          final double[] parms = new double[] {s0, k, r, b, t, sigma };
          final int n = parms.length;

          for (final boolean isCall : tfSet) {
            final double price = bs.price(s0, k, r, b, t, sigma, isCall);
            final double[] sense = bs.getPriceAdjoint(s0, k, r, b, t, sigma, isCall);
            assertEquals("price " + s0 + " " + r + " " + b + " " + isCall, price, sense[0], 1e-13);

            for (int i = 0; i < n; i++) {
              final double delta = (1 + Math.abs(parms[i])) * eps;
              final double[] temp = Arrays.copyOf(parms, n);
              temp[i] += delta;
              final double up = bs.price(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], isCall);
              temp[i] -= 2 * delta;
              final double down = bs.price(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], isCall);
              // System.out.println("debug " + i + " " + s0 + " " + r + " " + b + " " + isCall + "\t" + up + "\t" + price + "\t" + down + "\t" + delta);
              double fd;
              if ((i == 3 && Math.abs(b) < delta) || (i == 2 && !isCall && r == 0.) /* || (i == 2 && Math.abs(r) < delta) */) {
                // there is a discontinuity in the gradient at at b == 0 r != 0, and also for puts with r = 0 hence forward difference for the test
                if (isCall) {
                  fd = (up - price) / delta;
                } else {
                  fd = (price - down) / delta;
                }
                assertEquals(i + " " + s0 + " " + r + " " + b + " " + isCall, fd, sense[i + 1], Math.abs(fd) * 1e-4);
              } else {
                fd = (up - down) / 2 / delta;
                if (!isCall && r == 0 && b > 0) {
                  assertEquals(i + " " + s0 + " " + r + " " + b + " " + isCall, fd, sense[i + 1], Math.abs(fd) * 1e-4);
                } else {
                  assertEquals(i + " " + s0 + " " + r + " " + b + " " + isCall, fd, sense[i + 1], Math.abs(fd) * 1e-4);
                }
              }
            }
          }
        }
      }
    }
  }



  @Test
  // (enabled = false)
  public void deltaGammaTest() {
    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double[] s0Set = new double[] {60, 90, 100, 110, 160 };
    final double k = 100;
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, 0.11 };
    final double sigma = 0.35;
    final double t = 0.5;
    final boolean[] tfSet = new boolean[] {true, false };

    for (final double s0 : s0Set) {
      final double eps = 1e-5 * s0;
      for (final double b : bSet) {
        for (final boolean isCall : tfSet) {
          final double price = bs.price(s0, k, r, b, t, sigma, isCall);
          final double[] sense = bs.getPriceDeltaGamma(s0, k, r, b, t, sigma, isCall);
          assertEquals("price " + s0 + " " + b, price, sense[0], 1e-13);
          final double up = bs.price(s0 + eps, k, r, b, t, sigma, isCall);
          final double down = bs.price(s0 - eps, k, r, b, t, sigma, isCall);
          final double fd = (up - down) / 2 / eps;
          final double fd2 = (up + down - 2 * price) / eps / eps;
          assertEquals("delta " + s0 + " " + b, fd, sense[1], Math.abs(fd) * 1e-6);
          assertEquals("gamma " + s0 + " " + b, fd2, sense[2], Math.abs(fd2) * 1e-4);
        }
      }
    }
  }

  @Test
  public void phiTest() {
    final double s0 = 100;
    final double[] x2Set = new double[] {130, 150, 170 };
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, r };
    final double t1 = 0.5;
    final double sigma = 0.35;

    final double t = 2 * t1 / (Math.sqrt(5) - 1);
    final double[] gammaSet = new double[] {0, 1, 0.67, 1.87 };
    final double x1 = 133.0;

    for (final double x2 : x2Set) {
      for (final double b : bSet) {
        for (final double gamma : gammaSet) {

          final double[] parms = new double[] {s0, t, gamma, x1, x2, r, b, sigma };
          final int n = parms.length;

          final BjerksundStenslandModel bs = new BjerksundStenslandModel();
          final double impA = bs.getPhi(s0, t1, gamma, x1, x2, r, b, sigma);
          final double[] sense = bs.getPhiAdjoint(s0, t, gamma, x1, x2, r, b, sigma);

          // System.out.println(impA + "\t" + sense[0]);
          assertEquals(impA, sense[0], 1e-12);
          final double eps = 1e-5;
          for (int i = 0; i < 8; i++) {
            final double[] temp = Arrays.copyOf(parms, n);
            final double delta = (1 + Math.abs(parms[i])) * eps;
            temp[i] += delta;
            final double up = bs.getPhiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7])[0];
            temp[i] -= 2 * delta;
            final double down = bs.getPhiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7])[0];
            final double fd = (up - down) / 2 / delta;
            // System.out.println(fd + "\t" + sense[i + 1]);
            assertEquals(i + " " + x2 + " " + b + " " + gamma, fd, sense[i + 1], Math.abs(fd) * 2e-8);
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

    for (final double k : kSet) {
      for (final double b : bSet) {
        for (final double gamma : gammaSet) {

          final double[] parms = new double[] {s0, t, gamma, k, x2, x1, r, b, sigma };
          final int n = parms.length;

          final BjerksundStenslandModel bs = new BjerksundStenslandModel();
          final double impA = bs.getPsi(s0, t1, t, gamma, k, x2, x1, r, b, sigma);
          final double[] sense = bs.getPsiAdjoint(s0, t, gamma, k, x2, x1, r, b, sigma);

          assertEquals(impA, sense[0], 1e-12);
          // System.out.println(impA + "\t" + sense[0]);

          final double eps = 1e-5;
          for (int i = 0; i < n; i++) {
            final double[] temp = Arrays.copyOf(parms, n);
            final double delta = (1 + Math.abs(parms[i])) * eps;
            temp[i] += delta;
            final double up = bs.getPsiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7], temp[8])[0];
            temp[i] -= 2 * delta;
            final double down = bs.getPsiAdjoint(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7], temp[8])[0];
            final double fd = (up - down) / 2 / delta;
            // System.out.println(fd + "\t" + sense[i + 1]);

            assertEquals(i + " " + k + " " + b + " " + gamma, fd, sense[i + 1], Math.abs(fd) * 1e-4); // TODO would expect better agreement than this

          }
        }
      }
    }
  }

  @Test
  public void psiDeltaTest() {
    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
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

    final double eps = 1e-5 * s0;

    for (final double k : kSet) {
      for (final double b : bSet) {
        for (final double gamma : gammaSet) {

          final double psi = bs.getPsi(s0, t1, t, gamma, k, x2, x1, r, b, sigma);
          final double[] sense = bs.getPsiDelta(s0, t, gamma, k, x2, x1, r, b, sigma);
          // double psi = sense[0];
          assertEquals("psi", psi, sense[0], Math.abs(psi) * 1e-15);
          final double up = bs.getPsi(s0 + eps, t1, t, gamma, k, x2, x1, r, b, sigma);
          final double down = bs.getPsi(s0 - eps, t1, t, gamma, k, x2, x1, r, b, sigma);

          final double fd = (up - down) / 2 / eps;
          final double fd2 = (up + down - 2 * psi) / eps / eps;
          assertEquals("psi delta", fd, sense[1], Math.abs(fd) * 1e-6);
          assertEquals("psi gamma", fd2, sense[2], Math.abs(fd2) * 1e-4);
        }
      }
    }
  }

  @Test
  public void biVarNormTest() {

    final double rho = Math.sqrt(0.5 * (Math.sqrt(5) - 1));
    final double a = 1.2;
    final double b = -0.6;
    final double eps = 1e-5;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double cent = BIVARIATE_NORMAL.getCDF(new double[] {a, b, rho });
    final double[] sense = bs.bivariateNormDiv(a, b, true);
    final double aUp = BIVARIATE_NORMAL.getCDF(new double[] {a + eps, b, rho });
    final double aDown = BIVARIATE_NORMAL.getCDF(new double[] {a - eps, b, rho });
    final double bUp = BIVARIATE_NORMAL.getCDF(new double[] {a, b + eps, rho });
    final double bDown = BIVARIATE_NORMAL.getCDF(new double[] {a, b - eps, rho });
    final double aUpbUp = BIVARIATE_NORMAL.getCDF(new double[] {a + eps, b + eps, rho });
    final double aDownbDown = BIVARIATE_NORMAL.getCDF(new double[] {a - eps, b - eps, rho });
    final double aUpbDown = BIVARIATE_NORMAL.getCDF(new double[] {a + eps, b - eps, rho });
    final double aDownbUp = BIVARIATE_NORMAL.getCDF(new double[] {a - eps, b + eps, rho });

    // 1st
    double fd = (aUp - aDown) / 2 / eps;
    assertEquals("dB/da", fd, sense[0], Math.abs(fd) * 1e-5);
    fd = (bUp - bDown) / 2 / eps;
    assertEquals("dB/db", fd, sense[1], Math.abs(fd) * 1e-5);
    // 2nd
    fd = (aUp + aDown - 2 * cent) / eps / eps;
    assertEquals("d^2B/da^2", fd, sense[2], Math.abs(fd) * 1e-4);
    fd = (bUp + bDown - 2 * cent) / eps / eps;
    assertEquals("d^2B/db^2", fd, sense[3], Math.abs(fd) * 1e-5);
    fd = (aUpbUp + aDownbDown - aUpbDown - aDownbUp) / 4 / eps / eps;
    assertEquals("d^2B/dadb", fd, sense[4], Math.abs(fd) * 2e-4);
  }

  @Test
  public void alphaTest() {
    final double k = 123;
    final double x = 204;
    final double beta = 2.3;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();

    final double[] sense = bs.getAlphaAdjoint(k, x, beta);
    final double[] parms = new double[] {k, x, beta };
    final int n = parms.length;
    final double eps = 1e-5;
    for (int i = 0; i < n; i++) {
      final double[] temp = Arrays.copyOf(parms, n);
      temp[i] += eps;
      final double up = bs.getAlphaAdjoint(temp[0], temp[1], temp[2])[0];
      temp[i] -= 2 * eps;
      final double down = bs.getAlphaAdjoint(temp[0], temp[1], temp[2])[0];
      final double fd = (up - down) / 2 / eps;
      assertEquals(fd, sense[i + 1], Math.abs(fd) * 1e-8);
    }
  }

  @Test
  public void betaTest() {
    final double r = 0.1;
    final double b = 0.04;
    final double sigma = 0.3;
    final double sigmaSq = sigma * sigma;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();

    final double[] sense = bs.getBetaAdjoint(r, b, sigmaSq);
    final double[] parms = new double[] {r, b, sigmaSq };
    final int n = parms.length;
    final double eps = 1e-5;
    for (int i = 0; i < n; i++) {
      final double[] temp = Arrays.copyOf(parms, n);
      temp[i] += eps;
      final double up = bs.getBetaAdjoint(temp[0], temp[1], temp[2])[0];
      temp[i] -= 2 * eps;
      final double down = bs.getBetaAdjoint(temp[0], temp[1], temp[2])[0];
      final double fd = (up - down) / 2 / eps;

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

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (final double gamma : gammaSet) {
      for (final double b : bSet) {

        final double[] sense = bs.getLambdaAdjoint(gamma, r, b, sigmaSq);
        final double[] parms = new double[] {gamma, r, b, sigmaSq };
        final int n = parms.length;
        final double eps = 1e-5;
        for (int i = 0; i < n; i++) {
          final double[] temp = Arrays.copyOf(parms, n);
          temp[i] += eps;
          final double up = bs.getLambdaAdjoint(temp[0], temp[1], temp[2], temp[3])[0];
          temp[i] -= 2 * eps;
          final double down = bs.getLambdaAdjoint(temp[0], temp[1], temp[2], temp[3])[0];
          final double fd = (up - down) / 2 / eps;
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

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (final double gamma : gammaSet) {
      for (final double b : bSet) {

        final double[] sense = bs.getKappaAdjoint(gamma, b, sigmaSq);
        final double[] parms = new double[] {gamma, b, sigmaSq };
        final int n = parms.length;
        final double eps = 1e-5;
        for (int i = 0; i < n; i++) {
          final double[] temp = Arrays.copyOf(parms, n);
          temp[i] += eps;
          final double up = bs.getKappaAdjoint(temp[0], temp[1], temp[2])[0];
          temp[i] -= 2 * eps;
          final double down = bs.getKappaAdjoint(temp[0], temp[1], temp[2])[0];
          final double fd = (up - down) / 2 / eps;
          assertEquals(fd, sense[i + 1], Math.abs(fd) * 2e-8);
        }
      }
    }
  }

  @Test
  public void i1Test() {
    final double k = 100.04;
    final double[] rSet = {-0.03, 0.1 };
    final double[] bb = new double[] {-0.03, 0.04 };
    final double sigma = 0.3;
    final double t = 0.3;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (final double r : rSet) {
      for (final double b : bb) {

        if (r >= b) {

          final double[] sense = bs.getI1Adjoint(k, r, b, sigma, t);
          // System.out.println(sense[0]);
          final double[] parms = new double[] {k, r, b, sigma, t };
          final int n = parms.length;
          final double eps = 1e-5;
          for (int i = 0; i < n; i++) {
            final double[] temp = Arrays.copyOf(parms, n);
            temp[i] += eps;
            final double up = bs.getI1Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
            temp[i] -= 2 * eps;
            final double down = bs.getI1Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
            final double fd = (up - down) / 2 / eps;
            // System.out.println(up + "\t" + down);
            assertEquals(i + "\t" + r + "\t" + b, fd, sense[i + 1], Math.abs(fd) * 1e-5);
          }
        }
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

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (final double b : bb) {

      final double[] sense = bs.getI2Adjoint(k, r, b, sigma, t);
      final double[] parms = new double[] {k, r, b, sigma, t };
      final int n = parms.length;
      final double eps = 1e-5;
      for (int i = 0; i < n; i++) {
        final double[] temp = Arrays.copyOf(parms, n);
        temp[i] += eps;
        final double up = bs.getI2Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
        temp[i] -= 2 * eps;
        final double down = bs.getI2Adjoint(temp[0], temp[1], temp[2], temp[3], temp[4])[0];
        final double fd = (up - down) / 2 / eps;
        assertEquals(fd, sense[i + 1], Math.abs(fd) * 1e-7);
      }
    }
  }

  @Test
  public void phiDeltaTest() {
    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double s0 = 100;
    final double[] x2Set = new double[] {130, 150, 170 };
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, r };
    final double t1 = 0.5;
    final double sigma = 0.35;

    final double t = 2 * t1 / (Math.sqrt(5) - 1);
    final double[] gammaSet = new double[] {0, 1, 0.67, 1.87 };
    final double x1 = 133.0;

    final double eps = s0 * 1e-5;

    for (final double x2 : x2Set) {
      for (final double b : bSet) {
        for (final double gamma : gammaSet) {

          final double phi = bs.getPhi(s0, t1, gamma, x1, x2, r, b, sigma);
          final double[] sense = bs.getPhiDelta(s0, t, gamma, x1, x2, r, b, sigma);
          assertEquals(phi, sense[0], Math.abs(phi) * 1e-15);
          final double up = bs.getPhi(s0 + eps, t1, gamma, x1, x2, r, b, sigma);
          final double down = bs.getPhi(s0 - eps, t1, gamma, x1, x2, r, b, sigma);
          final double fd = (up - down) / 2 / eps;
          final double fd2 = (up + down - 2 * phi) / eps / eps;
          assertEquals(fd, sense[1], Math.abs(fd) * 2e-8);
          assertEquals(fd2, sense[2], Math.abs(fd2) * 1e-5);
        }
      }
    }
  }

  @Test
  public void function1DTest() {
    final double[] s0Set = new double[] {60, 90, 100, 110, 160 };
    final double k = 100;
    final double[] rSet = new double[] {0.0, 0.1 };
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, 0.11 };
    final double sigma = 0.35;
    final Expiry expiry = new Expiry(DateUtils.getDateOffsetWithYearFraction(DateUtils.getUTCDate(2011, 7, 1), 0.5));
    final double t = 0.5;
    final boolean[] tfSet = new boolean[] {true, false };

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();

    for (final double s0 : s0Set) {
      for (final double r : rSet) {
        for (final double b : bSet) {
          for (final boolean isCall : tfSet) {
            final double price = bs.price(s0, k, r, b, t, sigma, isCall);
            final AmericanVanillaOptionDefinition option = new AmericanVanillaOptionDefinition(k, expiry, isCall);
            final Function1D<StandardOptionDataBundle, Double> pFunc = bs.getPricingFunction(option);
            final StandardOptionDataBundle dataBundle = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(r)), b, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), s0,
                DateUtils.getUTCDate(2011, 7, 1));
            final double priceFunc = pFunc.evaluate(dataBundle);
            assertEquals(price, priceFunc, 1e-16);
          }
        }
      }
    }
  }

  @Test
  public void impliedVolSimpletest() {
    final double modSpot = 30.405;
    final double strike = 30.0;
    final double discountRate = 0.0023576132185433372;
    final double costOfCarry = 0.0023576132185433372;
    final double timeToExpiry = 0.010958904109589041;
    final double optionPrice = 0.275;
    final boolean isCall = false;

    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    final double vol = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall);
    final double vol1 = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall, 0.);
    final double vol2 = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall, 0.0001);
    final double vol3 = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall, 0.001);
    final double vol4 = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall, 0.01);
    final double vol5 = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall, 0.15);
    final double vol6 = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall, 0.5);
    final double vol7 = bs.impliedVolatility(optionPrice, modSpot, strike, discountRate, costOfCarry, timeToExpiry, isCall, 1.);
    assertEquals(vol, vol1, 1e-9);
    assertEquals(vol, vol2, 1e-9);
    assertEquals(vol, vol3, 1e-9);
    assertEquals(vol, vol4, 1e-9);
    assertEquals(vol, vol5, 1e-9);
    assertEquals(vol, vol6, 1e-9);
    assertEquals(vol, vol7, 1e-9);
    assertEquals(optionPrice, bs.price(modSpot, strike, discountRate, costOfCarry, timeToExpiry, vol, isCall), 1.e-5);
  }

  @Test
  void zeroVolTest() {
    double spot = 100;
    double strike = 96;
    double r = 0.01;
    double q = 0.07;
    double b = r - q;
    double t = 2.0;
    boolean isCall = false;
    final BjerksundStenslandModel bs = new BjerksundStenslandModel();
    double price = bs.price(spot, strike, r, b, t, 0.0, isCall);
    double expected = Math.exp(-r * t) * Math.max(strike - spot * Math.exp(b * t), 0.0);
    assertEquals(expected, price, 1e-14);
  }

}
