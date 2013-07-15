/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults appropriate
 * for pricing using an interpolated Black lognormal volatility surface.
 */
public class EquityOptionInterpolatedBlackLognormalPerExchangeDefaults extends EquityOptionInterpolatedBlackLognormalDefaults {

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perExchangeConfig Defaults values of curve configuration, discounting curve, surface name and interpolation method per exchange, not null
   */
  public EquityOptionInterpolatedBlackLognormalPerExchangeDefaults(final String priority, final String... perExchangeConfig) {
    super(priority, perExchangeConfig);
  }

  @Override
  protected String getId(final Security security) {
    final ExternalId exchange = FinancialSecurityUtils.getExchange(security);
    if (exchange == null) {
      return null;
    }
    return exchange.getValue().toUpperCase();
  }

}
