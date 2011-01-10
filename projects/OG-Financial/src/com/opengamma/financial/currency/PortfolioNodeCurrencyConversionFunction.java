/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.ComputationTargetType;

/**
 * Converts a value from one currency to another, acting on a portfolio node.
 */
public class PortfolioNodeCurrencyConversionFunction extends CurrencyConversionFunction {

  public PortfolioNodeCurrencyConversionFunction(final String valueName) {
    super(ComputationTargetType.PORTFOLIO_NODE, valueName);
  }

  public PortfolioNodeCurrencyConversionFunction(final String... valueNames) {
    super(ComputationTargetType.PORTFOLIO_NODE, valueNames);
  }

}
