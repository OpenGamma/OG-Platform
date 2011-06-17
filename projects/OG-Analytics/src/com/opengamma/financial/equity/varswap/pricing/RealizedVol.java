/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import com.opengamma.util.timeseries.DoubleTimeSeries;

import javax.time.calendar.ZonedDateTime;

/**
 * TODO: Implement RealizedVol. Not sure whether this will be a class or a static function or what, but I've added this to put notes down 
 */
public class RealizedVol {
  private final DoubleTimeSeries<ZonedDateTime> _timeSeries; // This will be provided to VarianceSwapDefinition.toDerivative() 

  /**
   * @param underlyingTimeSeries Contains historical values of the underlying. 
   */
  public RealizedVol(DoubleTimeSeries<ZonedDateTime> underlyingTimeSeries) {
    super();
    _timeSeries = underlyingTimeSeries;
  }

  public double get(double now) {
    return _timeSeries.getLatestValue(); // Place holder. Will in fact use subSeries, then go from there.
  }

}
