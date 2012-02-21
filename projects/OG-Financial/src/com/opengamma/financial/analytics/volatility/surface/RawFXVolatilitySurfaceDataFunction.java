/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class RawFXVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {

  public RawFXVolatilitySurfaceDataFunction(final String definitionName, final String specificationName) {
    super(definitionName, specificationName, "FX_VANILLA_OPTION");
  }

  @Override
  public boolean isCorrectIdType(final ComputationTarget target) {
    return UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

}
