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
   * Bond.
   */
  BOND,
  /**
   * Agricultural future.
   */
  AGRICULTURE_FUTURE,
  /**
   * Bond future.
   */
  BOND_FUTURE,
  /**
   * Bill.
   */
  BILL,
  /**
   * Cash.
   */
  CASH,
  /**
   * Credit Default Swap.
   */
  CREDIT_DEFAULT_SWAP,
  /**
   * Energy future.
   */
  ENERGY_FUTURE,
  /**
   * Equity dividend future.
   */
  EQUITY_DIVIDEND_FUTURE,
  /**
   * Equity.
   */
  EQUITY,
  /**
   * Umbrella for futures on underlying securities or indices in the Equity sector.
   */
  EQUITY_FUTURE,
  /**
   * Equity index.
   */
  EQUITY_INDEX,
  /**
   * Equity index dividend future.
   */
  EQUITY_INDEX_DIVIDEND_FUTURE_OPTION,
  /**
   * Equity index future option.
   */
  EQUITY_INDEX_FUTURE_OPTION,
  /**
   * Equity index option.
   */
  EQUITY_INDEX_OPTION,
  /**
   * Equity option.
   */
  EQUITY_OPTION,
  /**
   * Financial future.
   */
  FINANCIAL_FUTURE,
  /**
   * Fra.
   */
  FRA,
  /**
   * FX future.
   */
  FX_FUTURE,
  /**
   * Index
   */
  INDEX,
  /**
   * Index future.
   */
  INDEX_FUTURE,
  /**
   * Index option.
   */
  INDEX_OPTION,
  /**
   * Interest rate future.
   */
  INTEREST_RATE_FUTURE,
  /**
   * IR future option.
   */
  IR_FUTURE_OPTION,
  /**
   * Meta future.
   */
  METAL_FUTURE,
  /**
   * Stock future.
   */
  STOCK_FUTURE,
  /**
   * Default security type for swaps.
   */
  SWAP,
  /**
   * Basis Swap.
   */
  BASIS_SWAP,
  /**
   * Artificial security type for volatility quotes.
   */
  VOLATILITY_QUOTE,
  /**
   * Artificial security type for spot rates.
   */
  SPOT_RATE,
  /**
   * Artificial, default security type for rates.
   */
  RATE,
  /**
   * Artificial security type for forward cross rates, quoted as the difference from the spot rate.
   */
  FORWARD_CROSS,
  /**
   * FX forward.
   */
  FX_FORWARD,
  /**
   * Commodity Future Option.
   */
  COMMODITY_FUTURE_OPTION,
  /**
   * FX Future Option.
   */
  FX_FUTURE_OPTION,
  /**
   * Bond Future Option.
   */
  BOND_FUTURE_OPTION,
  /**
   * CD.
   */
  CD,
  /**
   * Inflation swap.
   */
  INFLATION_SWAP

}
