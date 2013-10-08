/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.core.config.Config;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
@Config(description = "Swaption volatility cube specification")
public class SwaptionVolatilityCubeSpecification {
  private final CubeInstrumentProvider<?, ?, ?> _cubeInstrumentProvider;
  private final String _name;
  private final String _cubeQuoteType;
  private final String _quoteUnits;
  private final UniqueIdentifiable _target;

  public SwaptionVolatilityCubeSpecification(final String name, final UniqueIdentifiable target, final String cubeQuoteType, final String quoteUnits,
      final CubeInstrumentProvider<?, ?, ?> cubeInstrumentProvider) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(cubeQuoteType, "cube quote type");
    ArgumentChecker.notNull(quoteUnits, "quote units");
    ArgumentChecker.notNull(cubeInstrumentProvider, "cube instrument provider");
    _name = name;
    _cubeQuoteType = cubeQuoteType;
    _quoteUnits = quoteUnits;
    _target = target;
    _cubeInstrumentProvider = cubeInstrumentProvider;
  }

  public CubeInstrumentProvider<?, ?, ?> getCubeInstrumentProvider() {
    return _cubeInstrumentProvider;
  }

  public String getName() {
    return _name;
  }

  public UniqueIdentifiable getTarget() {
    return _target;
  }

  public String getCubeQuoteType() {
    return _cubeQuoteType;
  }

  public String getQuoteUnits() {
    return _quoteUnits;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _cubeInstrumentProvider.hashCode();
    result = prime * result + _cubeQuoteType.hashCode();
    result = prime * result + _name.hashCode();
    result = prime * result + _quoteUnits.hashCode();
    result = prime * result + _target.hashCode();
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
    final SwaptionVolatilityCubeSpecification other = (SwaptionVolatilityCubeSpecification) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_target, other._target)) {
      return false;
    }
    if (!ObjectUtils.equals(_cubeInstrumentProvider, other._cubeInstrumentProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_cubeQuoteType, other._cubeQuoteType)) {
      return false;
    }
    if (!ObjectUtils.equals(_quoteUnits, other._quoteUnits)) {
      return false;
    }
    return true;
  }

}
