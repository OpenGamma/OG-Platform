/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;

import com.opengamma.id.ExternalId;

/**
 * Provides instruments for each point on the curve
 * @param <X> The type of the x-axis values
 */
//TODO probably best to combine this and CurveInstrumentProvider
public interface FuturePriceCurveInstrumentProvider<X> {

  ExternalId getInstrument(X xAxis);

  ExternalId getInstrument(X xAxis, LocalDate curveDate);

  String getDataFieldName();
  
  String getTickerScheme();
}
