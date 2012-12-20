/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.ComputationTargetType;

/**
 * Injects a default currency requirement into the graph at a portfolio node.
 */
public abstract class PortfolioNodeDefaultCurrencyFunction extends DefaultCurrencyFunction {

  protected PortfolioNodeDefaultCurrencyFunction(final boolean permitWithout, final String valueName) {
    super(ComputationTargetType.PORTFOLIO_NODE, permitWithout, valueName);
  }

  protected PortfolioNodeDefaultCurrencyFunction(final boolean permitWithout, final String... valueNames) {
    super(ComputationTargetType.PORTFOLIO_NODE, permitWithout, valueNames);
  }

  /**
   * Flagged to inject the default currency at a higher priority to avoid other functions
   * handling the currency omitted case. 
   */
  public static class Strict extends PortfolioNodeDefaultCurrencyFunction {

    public Strict(final String valueName) {
      super(false, valueName);
    }

    public Strict(final String... valueNames) {
      super(false, valueNames);
    }

  }

  /**
   * Flagged to inject the default currency at a lower priority to allow other functions to
   * handle the currency omitted case.
   */
  public static class Permissive extends PortfolioNodeDefaultCurrencyFunction {

    public Permissive(final String valueName) {
      super(true, valueName);
    }

    public Permissive(final String... valueNames) {
      super(true, valueNames);
    }

  }

}
