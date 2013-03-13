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
    final UniqueId uniqueId = UniqueId.parse(target.getValue().toString());
    //FIXME: Modify to take ExternalId to avoid incorrect cast to UniqueId
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(uniqueId);
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
