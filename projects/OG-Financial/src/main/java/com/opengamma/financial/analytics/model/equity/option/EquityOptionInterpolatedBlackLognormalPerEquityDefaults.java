/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.property.DefaultPropertyFunction;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults appropriate
 * for pricing using an interpolated Black lognormal volatility surface.
 */
public class EquityOptionInterpolatedBlackLognormalPerEquityDefaults extends EquityOptionInterpolatedBlackLognormalDefaults {

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perEquityConfig Defaults values of curve configuration, discounting curve, surface name and interpolation method per equity, not null
   */
  public EquityOptionInterpolatedBlackLognormalPerEquityDefaults(final String priority, final String... perEquityConfig) {
    super(priority, perEquityConfig);
  }

  @Override
  protected String getId(final Security security) {
    final String id = EquitySecurityUtils.getIndexOrEquityNameFromUnderlying(security, true);
    if (id != null) {
      return id.toUpperCase();
    }
    return null;
  }

}
