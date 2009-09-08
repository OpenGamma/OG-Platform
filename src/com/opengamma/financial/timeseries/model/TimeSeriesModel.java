package com.opengamma.financial.timeseries.model;

import java.util.List;

public interface TimeSeriesModel {

  /**
   * 
   * @param n
   *          Order of the model i.e. produces an AR(n) model
   * @param mean
   *          Mean value of series
   * @param phi
   *          List of coefficients. The number of coefficients must equal the
   *          order of the model
   * @param num
   *          Length of series to generate
   * @throws IllegalArgumentException
   * @return
   */
  public List<Double> getSeries(int n, double mean, List<Double> phi, int num);

  // TODO
  // public fitModel

  // TODO
  // public estimate parameters

  // TODO
  // public forecast
}
