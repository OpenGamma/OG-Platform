/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults appropriate
 * for pricing using an interpolated Black lognormal volatility surface.<p>
 * In this class, the inputs are keyed by SecurityType, a string available on all Security's. 
 * So, for example, for 'ForwardCurveCalculationMethod', EQUITY_OPTION's might use 'YieldCurveImplied' 
 * while EQUITY_INDEX_OPTION's might use 'FuturePriceMethod'
 */
public class EquityOptionInterpolatedBlackLognormalPerSecurityTypeDefaults extends EquityOptionInterpolatedBlackLognormalDefaults {

  public EquityOptionInterpolatedBlackLognormalPerSecurityTypeDefaults(final String priority, final String... perSecurityTypeConfig) {
    super(priority, perSecurityTypeConfig);
  }

  @Override
  protected String getId(Security security) {
    return security.getSecurityType();
  }

}
