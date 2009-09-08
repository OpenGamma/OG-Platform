package com.opengamma.financial.model.stochastic;

import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

public class BlackScholesMertonProcess implements StochasticProcess {
  private double _lnS;
  private double _sigma;
  private double _nu;
  private double _t;
  private ProbabilityDistribution<Double> _random = new NormalProbabilityDistribution(0, 1);

  public BlackScholesMertonProcess(double s, double t, double r, double b, double sigma) {
    _lnS = Math.log(s);
    _nu = r - b - 0.5 * sigma * sigma;
    _t = t;
    _sigma = sigma;
  }

  public double[] getPath(int n) {
    double dt = _t / n;
    double sigmaDt = _sigma * Math.sqrt(dt);
    double nuDt = _nu * dt;
    double x[] = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = Math.exp(_lnS + nuDt * sigmaDt * _random.nextRandom());
    }
    return x;
  }
}
