/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import java.util.Set;

import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.UnderlyingType;

public interface Sensitivity<T> {

  public T getSensitivity();

  public String getIdentifier();

  public int getOrder();

  public Set<UnderlyingType> getUnderlyings();

  public Underlying getUnderlying();
}
