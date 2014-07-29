/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CurveInstrumentConfig {
  private final Map<StripInstrumentType, String[]> _exposures;
  
  public CurveInstrumentConfig() {
    this(Collections.<StripInstrumentType, String[]>emptyMap());
  }

  public CurveInstrumentConfig(final Map<StripInstrumentType, String[]> exposures) {
    ArgumentChecker.notNull(exposures, "exposures");
    _exposures = exposures;
  }

  public Map<StripInstrumentType, String[]> getExposures() {
    return _exposures;
  }

  public String[] getExposuresForInstrument(final StripInstrumentType instrumentType) {
    final String[] exposures = _exposures.get(instrumentType);
    if (exposures == null) {
      throw new IllegalArgumentException("Could not get exposures for " + instrumentType);
    }
    return exposures;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _exposures.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CurveInstrumentConfig other = (CurveInstrumentConfig) obj;
    if (_exposures.size() != other._exposures.size()) {
      return false;
    }
    for (final Map.Entry<StripInstrumentType, String[]> entry : _exposures.entrySet()) {
      if (!other._exposures.containsKey(entry.getKey())) {
        return false;
      }
      if (!Arrays.deepEquals(entry.getValue(), _exposures.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

}
