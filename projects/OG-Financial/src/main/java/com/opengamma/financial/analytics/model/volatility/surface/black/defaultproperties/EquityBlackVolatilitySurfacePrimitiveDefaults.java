/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.id.UniqueId;

/**
 *
 */
public class EquityBlackVolatilitySurfacePrimitiveDefaults extends EquityBlackVolatilitySurfaceDefaults {

  public EquityBlackVolatilitySurfacePrimitiveDefaults(final String... defaultsPerTicker) {
    super(ComputationTargetType.PRIMITIVE, defaultsPerTicker);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId uniqueId = target.getUniqueId();
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(uniqueId);
    if (getAllTickers().contains(ticker)) {
      return true;
    }
    return false;
  }

  @Override
  protected String getTicker(final ComputationTarget target) {
    return EquitySecurityUtils.getIndexOrEquityName(target.getUniqueId());
  }
}
