package com.opengamma.financial.timeseries.model;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

//TODO this is just an AR(1) series at the moment
public class AutoregressiveModel implements TimeSeriesModel {
  // TODO change the seed to something sensible
  private RandomEngine _engine = new MersenneTwister(0);

  // TODO set variance of a(t)
  public List<Double> getSeries(int n, double mean, List<Double> phi, int num) {
    if (phi.size() != n)
      throw new IllegalArgumentException("Number of coefficients must equal the order of the model: have " + phi.size() + " coefficients for an AR(" + n + ") model");
    List<Double> series = new ArrayList<Double>();
    double sum = 0;
    for (double p : phi) {
      sum += p;
    }
    double phi0 = mean * (1 - sum);
    for (int i = 0; i < num; i++) {
      if (i < n) {
        series.add(phi0);
      } else {
        sum = phi0;
        for (int j = 0; j < n; j++) {
          sum += series.get(i - j - 1) * phi.get(j);
        }
        series.add(sum + _engine.nextDouble());
      }
    }
    return series;
  }
  // TODO another getSeries with an initial value of r(-1);
}
