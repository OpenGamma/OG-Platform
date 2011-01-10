/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

/**
 * This should be pulled from the configuration.
 */
public class StaticCurveInstrumentProvider implements CurveInstrumentProvider {
  private Identifier _identifier;
  
  public StaticCurveInstrumentProvider(Identifier identifier) {
    _identifier = identifier;
  }
  @Override
  public Identifier getInstrument(LocalDate curveDate, Tenor tenor) {
    return _identifier;
  }
  
  @Override
  public Identifier getInstrument(LocalDate curveDate, Tenor tenor, int numQuarterlyFuturesFromTenor) {
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
