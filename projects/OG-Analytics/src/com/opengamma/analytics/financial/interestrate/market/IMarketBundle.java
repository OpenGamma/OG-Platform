/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface of a "market" providing discounting factors, forward rate (linked to Ibor index), inflation price index and issuer specific curves.
 */
public interface IMarketBundle {

  /**
   * Create a new copy of the bundle.
   * @return The bundle.
   */
  IMarketBundle copy();

  /**
   * Gets the discount factor for one currency at a given time to maturity.
   * @param ccy The currency.
   * @param time The time.
   * @return The discount factor.
   */
  double getDiscountFactor(Currency ccy, Double time);

  /**
   * Return the name associated to the discounting in a currency.
   * @param ccy The currency.
   * @return The name.
   */
  String getName(Currency ccy);

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  double getForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @return The forward rate.
   */
  double getForwardRate(IborIndex index, double startTime);

  /**
   * Returns the name associated to the forward in an index.
   * @param index The index.
   * @return The name.
   */
  String getName(IborIndex index);

  /**
   * Gets the estimated price index for a given reference time.
   * @param index The price index.
   * @param time The reference time.
   * @return The price index.
   */
  double getPriceIndex(IndexPrice index, Double time);

  /**
   * Return the name associated to a price index.
   * @param index The price index.
   * @return The name.
   */
  String getName(IndexPrice index);

  /**
   * Gets the discount factor for one issuer in one currency.
   * @param issuerCcy The issuer name/currency pair.
   * @param time The time.
   * @return The discount factor.
   */
  double getDiscountFactor(Pair<String, Currency> issuerCcy, Double time);

}
