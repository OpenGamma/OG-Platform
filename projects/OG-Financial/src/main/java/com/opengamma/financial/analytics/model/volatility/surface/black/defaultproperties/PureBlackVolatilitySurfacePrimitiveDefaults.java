/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;

/**
 * Defaults for the volatility surface.
 */
public class PureBlackVolatilitySurfacePrimitiveDefaults extends PureBlackVolatilitySurfaceDefaults {

  /**
   * Creates an instance.
   * 
   * @param defaultsPerTicker  the defaults, not null
   */
  public PureBlackVolatilitySurfacePrimitiveDefaults(final String... defaultsPerTicker) {
    super(ComputationTargetType.PRIMITIVE, defaultsPerTicker);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getValue() instanceof ExternalIdentifiable)) {
      return false;
    }
    ExternalId id = ((ExternalIdentifiable) target.getValue()).getExternalId();
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(id);
    if (ticker == null) {
      return false;
    }
    if (getAllTickers().contains(ticker)) {
      return true;
    }
    return false;
  }

  @Override
  protected String getTicker(final ComputationTarget target) {
    return target.getUniqueId().getValue();
  }

}
