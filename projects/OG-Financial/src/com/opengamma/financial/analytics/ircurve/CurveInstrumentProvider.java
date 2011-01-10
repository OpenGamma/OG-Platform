/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.LocalDate;

import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public interface CurveInstrumentProvider {
  Identifier getInstrument(LocalDate curveDate, Tenor tenor);
  Identifier getInstrument(LocalDate curveDate, Tenor tenor, int numQuarterlyFuturesFromTenor);
}
