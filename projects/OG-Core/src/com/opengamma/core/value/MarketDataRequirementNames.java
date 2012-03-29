/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.value;

/**
 * A set of common names used to refer to market data values.
 */
public interface MarketDataRequirementNames {

  // All market data field names must be prefixed with "Market_" to distinguish them as market data fields in the
  // global namespace of field names. The field name that follows should be in Pascal case.

  /** 
   * Sent in the OpenGamma normalization schema for all asset types. Used to provide whatever value best represents the
   * instrument.
   */
  String MARKET_VALUE = "Market_Value";

  /** 
   * Sent in the OpenGamma normalization schema for all options for which it is available from the underlying market
   * data API. 
   */
  String IMPLIED_VOLATILITY = "Market_ImpliedVolatility";

  /** 
   * Sent in the OpenGamma normalization schema for all asset types for which available (e.g., equities). 
   */
  String VOLUME = "Market_Volume";

  /**
   * Last trade value
   */
  String LAST = "Market_Last";

  /**
   * Settlement value. This is used when one wants most recent close available in both Live and Historical 
   * Live - value of last trading day's settlement
   * Historical - value of specified day's settlement 
   */
  String SETTLE_PRICE = "Market_SettlementPrice";

  /**
   * (bid + ask) / 2
   */
  String MID = "Market_Mid";

  /**
   * Highest bid
   */
  String BID = "Market_Bid";

  /**
   * Lowest ask
   */
  String ASK = "Market_Ask";

  /**
   * Best estimate of implied volatility, might be same as mid implied vol
   */
  String BEST_IMPLIED_VOLATILITY = "Market_OptImpliedVolatilityBest";

  /**
   * Implied vol if you want to sell options
   */
  String BID_IMPLIED_VOLATILITY = "Market_OptImpliedVolatilityBid";

  /**
   * Implied vol if you want to buy options
   */
  String ASK_IMPLIED_VOLATILITY = "Market_OptImpliedVolatilityAsk";

  /**
   * Implied vol based on the last trade
   */
  String LAST_IMPLIED_VOLATILITY = "Market_OptImpliedVolatilityLast";

  /**
   * An average of bid and ask implied vol
   */
  String MID_IMPLIED_VOLATILITY = "Market_OptImpliedVolatilityMid";

  /**
   * An average of yield and the convention (?)
   */
  String YIELD_CONVENTION_MID = "Market_YieldConventionMid";

  /**
   * An average of yield and yield to maturity
   */
  String YIELD_YIELD_TO_MATURITY_MID = "Market_YieldYieldToMaturityMid";

  /**
   * Market dirty price (MID) 
   */
  String DIRTY_PRICE_MID = "Market_DirtyPriceMid";

  /**
   * Market estimated annual dividend yield
   */
  String DIVIDEND_YIELD = "Market_DividendYield";

  /**
   * Market estimated cost of carry, as continuous annual yield (interest - income)
   */
  String COST_OF_CARRY = "Market_CostOfCarry";

}
