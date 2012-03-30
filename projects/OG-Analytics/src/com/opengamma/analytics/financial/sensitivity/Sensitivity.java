/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.sensitivity;

import java.util.List;

import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.pnl.UnderlyingType;

/**
 * 
 * @param <T> Type of sensitivity
 */
public interface Sensitivity<T> {

  T getSensitivity();

  String getIdentifier();

  int getOrder();

  List<UnderlyingType> getUnderlyingTypes();

  Underlying getUnderlying();
}
