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
   * The date and time associated with {@link MarketDataRequirementNames#LAST}.
   */
  String LAST_DATE_TIME = "Market_LastDateTime";

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
   * The date and time associated with {@link MarketDataRequirementNames#BID}.
   */
  String BID_DATE_TIME = "Market_BidDateTime";

  /**
   * Lowest ask
   */
  String ASK = "Market_Ask";
  /**
   * The date and time associated with {@link MarketDataRequirementNames#ASK}.
   */
  String ASK_DATE_TIME = "Market_AskDateTime";

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
   * Market estimated annual dividend
   */
  String ANNUAL_DIVIDEND = "Market_AnnualDividend";
  
  /**
   * Market next dividend date
   */
  String NEXT_DIVIDEND_DATE = "Market_NextDividendDate";
  
  /**
   * Estimated frequency of dividend payments, as the number of payments per year
   */
  String DIVIDEND_FREQUENCY = "Market_DividendFrequency";

  /**
   * Market estimated cost of carry, as continuous annual yield (interest - income)
   */
  String COST_OF_CARRY = "Market_CostOfCarry";

  /**
   * High value. Sometimes sent as part of a candle or EOD message.
   */
  String HIGH = "Market_High";

  /**
   * Low value. Sometimes sent as part of a candle or EOD message.
   */
  String LOW = "Market_Low";

  /**
   * Previous day's closing bid price.
   */
  String CLOSING_BID = "Market_ClosingBid";
  /**
   * The date associated with {@link MarketDataRequirementNames#CLOSING_BID}.
   */
  String CLOSING_BID_DATE = "Market_ClosingBidDate";

  /**
   * Previous day's closing ask price.
   */
  String CLOSING_ASK = "Market_ClosingAsk";
  /**
   * The date associated with {@link MarketDataRequirementNames#CLOSING_ASK}.
   */
  String CLOSING_ASK_DATE = "Market_ClosingAskDate";
  
  /**
   * Last closing trade price, may be from mulitple days ago
   */
  String CLOSE = "Market_Close";
  /**
   * The date associated with {@link MarketDataRequirementNames#CLOSE}.
   */
  String CLOSE_DATE = "Market_CloseDate";
  
  /**
   * A special name used to request all available market data for an instrument.
   */
  String ALL = "Market_All";

}
