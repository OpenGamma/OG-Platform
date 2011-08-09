/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * This should be pulled from the configuration.
 */
public class StaticCurveInstrumentProvider implements CurveInstrumentProvider {
  private ExternalId _identifier;
  
  public StaticCurveInstrumentProvider(ExternalId identifier) {
    _identifier = identifier;
  }
  @Override
  public ExternalId getInstrument(LocalDate curveDate, Tenor tenor) {
    return _identifier;
  }
  
  @Override
  public ExternalId getInstrument(LocalDate curveDate, Tenor tenor, int numQuarterlyFuturesFromTenor) {
    throw new OpenGammaRuntimeException("futures not supported by this class");
  }
  
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof StaticCurveInstrumentProvider)) {
      return false;
    }
    StaticCurveInstrumentProvider other = (StaticCurveInstrumentProvider) o;
    return _identifier.equals(other._identifier);
  }
  
  public int hashCode() {
    return _identifier.hashCode();
  }
  
}
