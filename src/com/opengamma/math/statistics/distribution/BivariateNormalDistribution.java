package com.opengamma.math.statistics.distribution;

import cern.jet.stat.Probability;

public class BivariateNormalDistribution implements ProbabilityDistribution<Double[]> {
  private double TWO_PI = 2 * Math.PI;
  private double[][] _xx = new double[][] { new double[] { -0.932469514203152, -0.981560634246719, -0.9931285991850950 },
      new double[] { -0.661209386466265, -0.904117256370475, -0.9639719272779140 }, new double[] { -0.238619186083197, -0.769902674194305, -0.9122344282513260 },
      new double[] { 0.0000000000000000, -0.587317954286617, -0.8391169718222190 }, new double[] { 0.0000000000000000, -0.367831498998180, -0.7463319064601510 },
      new double[] { 0.0000000000000000, -0.125233408511469, -0.6360536807265150 }, new double[] { 0.0000000000000000, 0.0000000000000000, -0.5108670019508270 },
      new double[] { 0.0000000000000000, 0.0000000000000000, -0.3737060887154200 }, new double[] { 0.0000000000000000, 0.0000000000000000, -0.2277858511416450 },
      new double[] { 0.0000000000000000, 0.0000000000000000, -0.0765265211334973 } };
  private double[][] _w = new double[][] { new double[] { 0.171324492379170, 0.0471753363865118, 0.0176140071391521 },
      new double[] { 0.360761573048138, 0.1069393259953180, 0.0406014298003869 }, new double[] { 0.467913934572690, 0.1600783285433460, 0.0626720483341091 },
      new double[] { 0.000000000000000, 0.2031674267230660, 0.0832767415767048 }, new double[] { 0.000000000000000, 0.2334925365383550, 0.1019301198172400 },
      new double[] { 0.000000000000000, 0.2491470458134030, 0.1181945319615180 }, new double[] { 0.000000000000000, 0.0000000000000000, 0.1316886384491770 },
      new double[] { 0.000000000000000, 0.0000000000000000, 0.1420961093183820 }, new double[] { 0.000000000000000, 0.0000000000000000, 0.1491729864726040 },
      new double[] { 0.000000000000000, 0.0000000000000000, 0.1527533871307260 } };

  public double getCDF(Double[] args) {
    double x = -args[0];
    double y = -args[1];
    double rho = args[2];
    double xy = x * y;
    double bvn = 0;
    int ng = 2;
    int lg = 9;
    if (Math.abs(rho) < 0.3) {
      ng = 0;
      lg = 2;
    } else if (Math.abs(rho) < 0.75) {
      ng = 1;
      lg = 5;
    }
    if (Math.abs(rho) < 0.925) {
      if (Math.abs(rho) > 0) {
        double xySquared = (x * x + y * y) / 2.;
        double asinRho = Math.asin(rho);
        for (int i = 0; i <= lg; i++) {
          bvn += getCorrection(asinRho, -1, i, ng, xy, xySquared);
          bvn += getCorrection(asinRho, 1, i, ng, xy, xySquared);
        }
        bvn *= asinRho / (2 * TWO_PI);
      }
      bvn += Probability.normal(-x) * Probability.normal(-y);
    } else {
      if (rho < 0) {
        y = -y;
        xy = -xy;
      }
      if (Math.abs(rho) < 1) {
        double aSquared = 1 - rho * rho;
        double a = Math.sqrt(aSquared);
        double bSquared = (x - y) * (x - y);
        double c = (4 - xy) / 8.;
        double d = (12 - xy) / 16.;
        double e = -(bSquared / aSquared + xy) / 2.;
        if (e > -100) {
          bvn = a * Math.exp(e) * (1 - c * (bSquared - aSquared) * (1 - d * bSquared / 5.) / 3. + c * d * aSquared * aSquared / 5.);
        }
        if (-xy < 100) {
          double b = Math.sqrt(bSquared);
          bvn = bvn - Math.exp(-xy / 2.) * Math.sqrt(TWO_PI) * Probability.normal(-b / a) * b * (1 - c * bSquared * (1 - d * bSquared / 5.) / 3.);
        }
        a /= 2.;
        for (int i = 0; i < lg; i++) {
          bvn += getCorrection(a, bSquared, c, d, -1, i, ng, xy);
          bvn += getCorrection(a, bSquared, c, d, 1, i, ng, xy);
        }
        bvn /= -TWO_PI;
      }
      if (rho > 0) {
        bvn += Probability.normal(-Math.max(x, y));
      } else {
        bvn = -bvn;
        if (y > x) {
          bvn += Probability.normal(y) - Probability.normal(x);
        }
      }
    }
    return bvn;
  }

  private double getCorrection(double a, int multiplier, int row, int column, double xy, double xySquared) {
    double sin = Math.sin(a * (multiplier * _xx[row][column] + 1) / 2.);
    return _w[row][column] * Math.exp((sin * xy - xySquared) / (1 - sin * sin));
  }

  private double getCorrection(double a, double b, double c, double d, int multiplier, int row, int column, double xy) {
    double xs = Math.pow(a * (multiplier * _xx[row][column] + 1), 2);
    double rs = Math.sqrt(1 - xs);
    double e = -(b / xs + xy) / 2.;
    if (e > -100) {
      return a * _w[row][column] * Math.exp(e) * (Math.exp(-xy * (1 - rs) / (2 * (1 + rs))) / rs - (1 + c * xs * (1 + d * xs)));
    }
    return 0;
  }

  @Override
  public double getPDF(Double[] x) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public double nextRandom() {
    // TODO
    throw new UnsupportedOperationException();
  }
}
