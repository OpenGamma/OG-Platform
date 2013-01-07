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
public class EquityBlackVolatilitySurfacePerCurrencySecurityDefaults extends EquityBlackVolatilitySurfaceDefaults {

  /**
   * @param priority The priority of these defaults
   * @param defaultsPerCurrency The default values for each currency
   */
  public EquityBlackVolatilitySurfacePerCurrencySecurityDefaults(final String priority, final String... defaultsPerCurrency) {
    super(ComputationTargetType.SECURITY, priority, defaultsPerCurrency);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof EquityIndexOptionSecurity) && !(security instanceof EquityOptionSecurity)) {
      return false;
    }
    final String ccy = FinancialSecurityUtils.getCurrency(security).getCode();
    if (getAllIds().contains(ccy)) {
      return true;
    }
    return false;
  }

  @Override
  protected String getId(final ComputationTarget target) {
    return FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
  }
}
