/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import java.util.Set;

import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.UnderlyingType;

/**
 * 
 * @param <T> Type of sensitivity
 */
public interface Sensitivity<T> {

  T getSensitivity();

  String getIdentifier();

  int getOrder();

  Set<UnderlyingType> getUnderlyingTypes();

  Underlying getUnderlying();
}
