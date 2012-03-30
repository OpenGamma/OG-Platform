/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.pricing;

import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;

/** TODO CASE : Review whether this enum is used anywhere
 * Methods available for the pricing and risk management of Equity Futures. See {@link EquityFutureDefinition} <p>
 * Available: MARK_TO_MARKET, COST_OF_CARRY, DIVIDEND_YIELD 
 */
public enum EquityFuturesPricingMethod {
  /** Pricing via published (or computed) market futures price. See {@link EquityFutureMarkToMarket}*/
  MARK_TO_MARKET,
  /** Pricing given the spot underlying and a scalar cost of Carry.  See {@link EquityFutureCostOfCarry}*/
  COST_OF_CARRY,
  /** Pricing from a spot underlying asset, a scalar deterministic continuous dividend yield of the asset, and a YieldAndDiscountCurve */
  DIVIDEND_YIELD
}
