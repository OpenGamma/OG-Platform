/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;

/**
 * Populates {@link ListedEquityOptionFunction} with defaults appropriate
 * for pricing using a volatility implied from the market value of the instrument.<p>
 * In this class, the inputs are keyed by underlying Ticker. 
 * See {@link EquitySecurityUtils#getIndexOrEquityNameFromUnderlying}
 */
public class ListedEquityOptionPerTickerDefaults extends ListedEquityOptionDefaults {

  public ListedEquityOptionPerTickerDefaults(String priority, String[] perIdConfig) {
    super(priority, perIdConfig);
  }

  @Override
  protected String getId(Security security) {
    final String id = EquitySecurityUtils.getIndexOrEquityNameFromUnderlying(security, true);
    if (id != null) {
      return id.toUpperCase();
    }
    return null;
  }

}
