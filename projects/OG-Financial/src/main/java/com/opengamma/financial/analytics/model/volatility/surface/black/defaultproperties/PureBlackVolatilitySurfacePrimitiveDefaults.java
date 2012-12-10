/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.id.UniqueId;

/**
 *
 */
public class PureBlackVolatilitySurfacePrimitiveDefaults extends PureBlackVolatilitySurfaceDefaults {

  public PureBlackVolatilitySurfacePrimitiveDefaults(final String... defaultsPerTicker) {
    super(ComputationTargetType.PRIMITIVE, defaultsPerTicker);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId uniqueId = target.getUniqueId();
    final String targetScheme = uniqueId.getScheme();
    if (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) || targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
      final String ticker = uniqueId.getValue();
      if (getAllTickers().contains(ticker)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected String getTicker(final ComputationTarget target) {
    return target.getUniqueId().getValue();
  }

}
