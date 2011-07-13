/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * Specification for a volatility surface - contains all available points on the surface.
 */
public class VolatilitySurfaceSpecification {
  private SurfaceInstrumentProvider<?, ?> _surfaceInstrumentProvider;
  private String _name;
  private UniqueIdentifiable _target;

  public VolatilitySurfaceSpecification(String name, UniqueIdentifiable target, SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider) {
    _name = name;
    _target = target;
    _surfaceInstrumentProvider = surfaceInstrumentProvider;
  }
  
  public SurfaceInstrumentProvider<?, ?> getSurfaceInstrumentProvider() {
    return _surfaceInstrumentProvider;
  }
  
  public String getName() {
    return _name;
  }
  
  /**
   * @deprecated use getTarget()
   * @throws ClassCastException if target not a currency
   * @return currency assuming that the target is a currency
   */
  @Deprecated
  public Currency getCurrency() {
    return (Currency) _target;
  }
  
  public UniqueIdentifiable getTarget() {
    return _target;
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
           other.getTarget().equals(getTarget()) &&
           other.getSurfaceInstrumentProvider().equals(getSurfaceInstrumentProvider());
  }
  
  public int hashCode() {
    return getName().hashCode() * getTarget().hashCode();
  }
}
