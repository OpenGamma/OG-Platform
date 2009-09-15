package com.opengamma.demo.timeseries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import cern.jet.random.Distributions;
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.timeseries.model.AutoregressiveModel;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

public class TimeSeriesDemoData {
  final RandomEngine _engine1 = new MersenneTwister(new Date());
  final RandomEngine _engine2 = new MersenneTwister(new Date());

  protected Double getDataPoint() {
    return null;
  }

  // TODO one with start and end dates
  public DoubleTimeSeries getTimeSeries(int n) {
    if (getDataPoint() == null) {
      return ArrayDoubleTimeSeries.EMPTY_SERIES;
    }
    LocalDate dateProvider = LocalDate.date(2000, 1, 1);
    TimeProvider timeProvider = LocalTime.time(0, 0);
    TimeZone timeZone = TimeZone.timeZone(ZoneOffset.zoneOffset(0));
    List<InstantProvider> dates = new ArrayList<InstantProvider>();
    List<Double> data = new ArrayList<Double>();
    InstantProvider start = ZonedDateTime.dateTime(dateProvider, timeProvider, timeZone);
    for (int i = 0; i < n; i++) {
      dates.add(start);
      data.add(getDataPoint());
      dateProvider = dateProvider.plusDays(1L);
      start = ZonedDateTime.dateTime(dateProvider, timeProvider, timeZone);
    }
    return new ArrayDoubleTimeSeries(dates, data);
  }
}

class NormalDistributionTimeSeriesData extends TimeSeriesDemoData {
  private final Normal _generator;
  private final double _mu;
  private final double _sigma;

  public NormalDistributionTimeSeriesData(double mu, double sigma) {
    _generator = new Normal(mu, sigma, _engine1);
    _mu = mu;
    _sigma = sigma;
  }

  @Override
  protected Double getDataPoint() {
    return _generator.nextDouble();
  }

  @Override
  public String toString() {
    return "Normal(" + _mu + ", " + _sigma + ")";
  }
}

/*
 * class ScaleMixtureNormalDistributionTimeSeriesData extends TimeSeriesDemoData
 * { private Normal _generator1; private Normal _generator2; private double
 * _mu1; private double _sigma1; private double _mu2; private double _sigma2;
 * private double _alpha;
 * 
 * public ScaleMixtureNormalDistributionTimeSeriesData(double mu1, double
 * sigma1, double mu2, double sigma2, double alpha) { _generator1 = new
 * Normal(mu1, sigma1, _engine1); _generator2 = new Normal(mu2, sigma2,
 * _engine2); _mu1 = mu1; _sigma1 = sigma1; _mu2 = mu2; _sigma2 = sigma2; _alpha
 * = alpha; }
 * 
 * @Override protected Double getDataPoint() { return _generator.nextDouble(); }
 * 
 * @Override public String toString() { return "Normal(" + _mu + ", " + _sigma +
 * ")"; } }
 */

class WeibullDistributionTimeSeriesData extends TimeSeriesDemoData {
  private final double _alpha;
  private final double _beta;

  public WeibullDistributionTimeSeriesData(double alpha, double beta) {
    _alpha = alpha;
    _beta = beta;
  }

  @Override
  protected Double getDataPoint() {
    return Distributions.nextWeibull(_alpha, _beta, _engine1);
  }

  @Override
  public String toString() {
    return "Weibull(" + _alpha + ", " + _beta + ")";
  }
}

class CauchyDistributionTimeSeriesData extends TimeSeriesDemoData {
  private final double _cutoff;

  public CauchyDistributionTimeSeriesData(double cutoff) {
    _cutoff = cutoff;
  }

  @Override
  protected Double getDataPoint() {
    double result = Distributions.nextCauchy(_engine1);
    while (Math.abs(result) > _cutoff) {
      result = Distributions.nextCauchy(_engine1);
    }
    return result;
  }

  @Override
  public String toString() {
    return "Cauchy(0, 1), cutoff = " + _cutoff;
  }
}

class AutoregressiveTimeSeriesData extends TimeSeriesDemoData {
  private final List<Double> _data;
  private int _count = 0;
  private final int _order;
  private final List<Double> _phi;

  public AutoregressiveTimeSeriesData(int order, int n, List<Double> phi) {
    AutoregressiveModel model = new AutoregressiveModel();
    _order = order;
    _phi = phi;
    _data = model.getSeries(order, 0, phi, n + 1);
  }

  @Override
  protected Double getDataPoint() {
    return _data.get(_count++);
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer("AR(" + _order + "), {");
    String phiString = "phi";
    for (int i = 1; i <= _order; i++) {
      result.append(phiString + "(i)"); // TODO subscript
    }
    // TODO put values from phi into string
    result.append("}");
    return result.toString();
  }
}
