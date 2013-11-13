/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;

/**
 * Populates {@link ListedEquityOptionFunction} with defaults appropriate
 * for pricing using a volatility implied from the market value of the instrument.<p>
 * In this class, the inputs are keyed by SecurityType, a string available on all Security's. 
 * So, for example, for 'ForwardCurveCalculationMethod', EQUITY_OPTION's might use 'YieldCurveImplied' 
 * while EQUITY_INDEX_OPTION's might use 'FuturePriceMethod'
 */
public class ListedEquityOptionPerSecurityTypeDefaults extends ListedEquityOptionDefaults {

  public ListedEquityOptionPerSecurityTypeDefaults(String priority, String[] perIdConfig) {
    super(priority, perIdConfig);
  }

  @Override
  protected String getId(Security security) {
    return security.getSecurityType();
  }

}
