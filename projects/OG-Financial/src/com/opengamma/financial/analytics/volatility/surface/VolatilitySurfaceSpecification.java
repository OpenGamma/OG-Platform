/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.common.Currency;

/**
 * Specification for a volatility surface - contains all available points on the surface.
 */
public class VolatilitySurfaceSpecification {
  private SurfaceInstrumentProvider<?, ?> _surfaceInstrumentProvider;
  private String _name;
  private Currency _currency;

  public VolatilitySurfaceSpecification(String name, Currency currency, SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider) {
    _name = name;
    _currency = currency;
    _surfaceInstrumentProvider = surfaceInstrumentProvider;
  }
  
  public SurfaceInstrumentProvider<?, ?> getSurfaceInstrumentProvider() {
    return _surfaceInstrumentProvider;
  }
  
  public String getName() {
    return _name;
  }
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof VolatilitySurfaceSpecification)) {
      return false;
    }
    VolatilitySurfaceSpecification other = (VolatilitySurfaceSpecification) o;
    return other.getName().equals(getName()) &&
           other.getCurrency().equals(getCurrency()) &&
           other.getSurfaceInstrumentProvider().equals(getSurfaceInstrumentProvider());
  }
  
  public int hashCode() {
    return getName().hashCode() * getCurrency().hashCode();
  }
}
