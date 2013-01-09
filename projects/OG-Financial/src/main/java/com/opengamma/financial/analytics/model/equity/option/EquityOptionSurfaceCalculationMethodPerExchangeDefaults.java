/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults appropriate
 * for pricing using an interpolated Black lognormal volatility surface.
 */
public class EquityOptionSurfaceCalculationMethodPerExchangeDefaults extends EquityOptionSurfaceCalculationMethodDefaults {

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perExchangeConfig Defaults values of curve configuration, discounting curve, surface name and interpolation method per equity, not null
   */
  public EquityOptionSurfaceCalculationMethodPerExchangeDefaults(final String priority, final String... perExchangeConfig) {
    super(priority, perExchangeConfig);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security eqSec = target.getSecurity();
    if (!((eqSec instanceof EquityIndexOptionSecurity) || (eqSec instanceof EquityBarrierOptionSecurity) || (eqSec instanceof EquityOptionSecurity))) {
      return false;
    }
    final String exchange = FinancialSecurityUtils.getExchange(eqSec).getValue();
    return getAllIds().contains(exchange);
  }

  @Override
  protected String getId(final Security security) {
    return FinancialSecurityUtils.getExchange(security).getValue().toUpperCase();
  }

}
