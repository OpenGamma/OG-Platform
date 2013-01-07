/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 *
 */
public class EquityBlackVolatilitySurfacePerExchangeTradeDefaults extends EquityBlackVolatilitySurfaceDefaults {

  /**
   * @param priority The priority of these defaults
   * @param defaultsPerExchange The default values per exchange
   */
  public EquityBlackVolatilitySurfacePerExchangeTradeDefaults(final String priority, final String... defaultsPerExchange) {
    super(ComputationTargetType.TRADE, priority, defaultsPerExchange);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    final Security security = target.getTrade().getSecurity();
    if (!(security instanceof EquityIndexOptionSecurity) && !(security instanceof EquityOptionSecurity)) {
      return false;
    }
    final String exchange = FinancialSecurityUtils.getExchange(security).getValue();
    if (getAllIds().contains(exchange)) {
      return true;
    }
    return false;
  }

  @Override
  protected String getId(final ComputationTarget target) {
    return FinancialSecurityUtils.getExchange(target.getTrade().getSecurity()).getValue();
  }
}
