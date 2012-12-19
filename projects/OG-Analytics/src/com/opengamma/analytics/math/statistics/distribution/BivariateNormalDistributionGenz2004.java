/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

/**
 * The implementation of the cdf is taken from
 * <p>
 * A.Genz, “Numerical computation of rectangular bivariate and trivariate normal and t probabilities”,
 * Statistics and Computing,  14, (3), 2004.
 * (<a href="http://link.springer.com/article/10.1023/B%3ASTCO.0000035304.20635.31">link</a>).
 */
public final class BivariateNormalDistributionGenz2004 extends BivariateNormalDistribution {

  private interface UnaryFunction {
    double evaluate(double x);
  }

  private static class GenzEqn3 implements UnaryFunction {

    private final double _hk, _hs, _halfU;

    public GenzEqn3(final double h, final double k, final double halfU) {
      this._hk = h * k;
      this._hs = ((h * h) + (k * k)) / 2.0;
      this._halfU = halfU;
    }

    @Override
    public double evaluate(final double x) {
      // -1<x<1, so transform to 0<theta<U
      final double theta = this._halfU * (x + 1.0);
      final double sn = Math.sin(theta);
      final double cscs = 1.0 - (sn * sn); // cos^2(theta)
      return Math.exp(((sn * this._hk) - this._hs) / cscs);
    }
  }

  private static class GenzEqn6 implements UnaryFunction {

    private final double _bs, _hk, _a, _c, _d;

    public GenzEqn6(final double bs, final double hk, final double a, final double c, final double d)
    {
      this._bs = bs;
      this._hk = hk;
      this._a = a;
      this._c = c;
      this._d = d;
    }

    @Override
    public double evaluate(final double z) {
      double x = this._a * (z + 1.0);
      double xs = x * x;
      final double rs = Math.sqrt(1 - xs);
      final double asr = -((this._bs / xs) + this._hk) / 2.0;
      if (asr > -100) {
        return Math.exp(asr)
            * ((Math.exp((-this._hk * (1 - rs)) / (2.0 * (1 + rs))) / rs) - (1.0 + (this._c * xs * (1.0 + (this._d * xs)))));
      }
      return 0;
    }

  }

  // Weights and Abcissa for Gauss-Legendre 6-point quadrature
  private static final double[] W6 = {0.1713244923791705E+00, 0.3607615730481384E+00, 0.4679139345726904E+00 };

  private static final double[] X6 = {-0.9324695142031522E+00, -0.6612093864662647E+00, -0.2386191860831970E+00 };

  // Weights and Abcissa for Gauss-Legendre 12-point quadrature
  private static final double[] W12 = {0.4717533638651177E-01, 0.1069393259953183E+00, 0.1600783285433464E+00,
    0.2031674267230659E+00, 0.2334925365383547E+00, 0.2491470458134029E+00 };

  private static final double[] X12 = {-0.9815606342467191E+00, -0.9041172563704750E+00, -0.7699026741943050E+00,
    -0.5873179542866171E+00, -0.3678314989981802E+00, -0.1252334085114692E+00 };

  // Weights and Abcissa for Gauss-Legendre 20-point quadrature
  private static final double[] W20 = {0.1761400713915212E-01, 0.4060142980038694E-01, 0.6267204833410906E-01,
    0.8327674157670475E-01, 0.1019301198172404E+00, 0.1181945319615184E+00, 0.1316886384491766E+00,
    0.1420961093183821E+00, 0.1491729864726037E+00, 0.1527533871307259E+00 };

  private static final double[] X20 = {-0.9931285991850949E+00, -0.9639719272779138E+00, -0.9122344282513259E+00,
    -0.8391169718222188E+00, -0.7463319064601508E+00, -0.6360536807265150E+00, -0.5108670019508271E+00,
    -0.3737060887154196E+00, -0.2277858511416451E+00, -0.7652652113349733E-01 };

  /** Approx 6.283 **/
  private static final double TWOPI = 2.0 * Math.PI;

  /** Approx 2.507 */
  private static final double SQRT_TWOPI = Math.sqrt(TWOPI);

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private double cnd(final double x) {
    return NORMAL.getCDF(x);
  }

  @Override
  protected double getCDF(final double x, final double y, final double rho) {

    final double absRho = Math.abs(rho);
    double[] wgt = W20;
    double[] abc = X20;

    if (absRho < 0.3) {
      wgt = W6;
      abc = X6;
    } else if (absRho < 0.75) {
      wgt = W12;
      abc = X12;
    }

    double h = -x;
    double k = -y;

    double bvn = 0.0;

    if (absRho < 0.925) {
      if (absRho > 0) {
        final double halfAsinRho = Math.asin(rho) / 2.0;
        UnaryFunction f = new GenzEqn3(h, k, halfAsinRho);
        bvn = (halfAsinRho * integrate(wgt, abc, f)) / TWOPI;
      }
      bvn += (cnd(-h) * cnd(-k));

    } else {

      double hk = h * k;
      if (rho < 0.0) {
        // handle s=sign(rho)
        k *= -1.0;
        hk *= -1.0;
      }

      if (absRho < 1.0) {
        final double as = (1 - rho) * (1 + rho);
        final double a = Math.sqrt(as);
        final double bs = (h - k) * (h - k);
        final double c = (4.0 - hk) / 8.0;
        final double d = (12.0 - hk) / 16.0;
        final double asr = -((bs / as) + hk) / 2.0;
        // Drezner and Wesolowsky analytic integral
        if (asr > -100.0) {
          bvn = a * Math.exp(asr)
              * ((1 - ((c * (bs - as) * (1 - ((d * bs) / 5.0))) / 3.0)) + ((c * d * as * as) / 5.0));
        }
        if (-hk < 100.0) {
          final double b = Math.sqrt(bs);
          bvn -= Math.exp(-hk / 2.0) * SQRT_TWOPI * cnd(-b / a) * b
              * (1.0 - ((c * bs * (1.0 - ((d * bs) / 5.0))) / 3.0));
        }
        final double halfa = a / 2.0;
        GenzEqn6 f = new GenzEqn6(bs, hk, halfa, c, d);
        bvn += halfa * integrate(wgt, abc, f);
        bvn /= -TWOPI;

      }
      if (rho > 0) {
        bvn += cnd(-Math.max(h, k));
      } else {
        bvn *= -1.0;
        if (k > h) {
          bvn += cnd(k) - cnd(h);
        }
      }
    }
    return bvn;
  }

  private double integrate(final double[] wgt, final double[] abc, final UnaryFunction f)
  {
    double y = 0.0;
    for (int i = 0; i < wgt.length; ++i) {
      y += wgt[i] * f.evaluate(abc[i]);
      y += wgt[i] * f.evaluate(-abc[i]);
    }
    return y;
  }
}
