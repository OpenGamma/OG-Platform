/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.defaultproperties;

import com.opengamma.engine.ComputationTargetType;

/**
 *
 */
public class LocalVolatilitySurfaceTradeDefaults extends LocalVolatilitySurfaceDefaults {

  public LocalVolatilitySurfaceTradeDefaults(final String eps) {
    super(ComputationTargetType.TRADE, eps);
  }

}
