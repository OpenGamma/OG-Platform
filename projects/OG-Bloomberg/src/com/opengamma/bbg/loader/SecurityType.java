/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import com.opengamma.bbg.livedata.normalization.BloombergRateRuleProvider;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Maps to {@link ManageableSecurity}.getSecurityType()
 * <p>
 * Beware that market data normalisation is performed by security type, so introducing new ones or changing the
 * boundaries could have unintended consequences. See {@link BloombergRateRuleProvider}.
 */
public enum SecurityType {
  /**
   * 
   */
  BOND,
  /**
   * 
   */
  AGRICULTURE_FUTURE,
  /**
   * 
   */
  BOND_FUTURE,
  /**
   * Cash
   */
  CASH,
  /**
   * 
   */
  ENERGY_FUTURE,
  /**
   * 
   */
  EQUITY_DIVIDEND_FUTURE,
  /**
   * 
   */
  EQUITY,
  /** Umbrella for futures on underlying securities or indices in the Equity sector */
  EQUITY_FUTURE,
  /**
   *
   */
  EQUITY_INDEX,
  /**
   *
   */
  EQUITY_INDEX_OPTION,
  /**
   * 
   */
  EQUITY_OPTION,
  /**
   * 
   */
  FINANCIAL_FUTURE,
  /**
   *
   */
  FRA,
  /**
   *
   */
  FX_FUTURE,
  /**
   * 
   */
  INDEX_FUTURE,
  /**
   * 
   */
  INDEX_OPTION,
  /**
   * 
   */
  INTEREST_RATE_FUTURE,
  /**
   *
   */
  IR_FUTURE_OPTION,
  /**
   * 
   */
  METAL_FUTURE,
  /**
   * 
   */
  STOCK_FUTURE,
  /**
   * Default security type for swaps
   */
  SWAP,
  /**
   * Basis Swap
   */
  BASIS_SWAP,
  /**
   * Artificial security type for volatility quotes 
   */
  VOLATILITY_QUOTE,
  /**
   * Artificial security type for spot rates
   */
  SPOT_RATE,
  /**
   * Artificial, default security type for rates
   */
  RATE,
  /**
   * Artificial security type for forward cross rates, quoted as the difference from the spot rate.
   */
  FORWARD_CROSS;
}
