/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

/**
 * Constants to be used in PnL Functions
 */
public final class PnLFunctionUtils {

  /** Constraint used in MarkToMarketPnLFunction and MarkToMarketPnLAliasFunction */
  public static final String PNL_TRADE_TYPE_CONSTRAINT = "TradeType";
  
  /** Constrains PNL to all trades that have been executed on the valuation date */
  public static final String PNL_TRADE_TYPE_NEW = "New";
  
  /** Constrains PNL to all trades in the portfolio that existed at the Open of trading on the valuation date */
  public static final String PNL_TRADE_TYPE_OPEN = "Open";
  
  /** Does not constrains PNL to a subset of trades. All trades are included. 
   * This equals the net of NEW and OPEN 
   */
  public static final String PNL_TRADE_TYPE_ALL = "All";
}
