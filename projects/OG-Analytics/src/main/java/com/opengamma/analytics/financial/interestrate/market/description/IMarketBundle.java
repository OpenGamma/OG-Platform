/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.util.money.Currency;
import com.opengamma.lambdava.tuple.DoublesPair;
import com.opengamma.lambdava.tuple.Pair;

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
   * Returns a set of all the currencies available for discounting.
   * @return The currency set.
   */
  Set<Currency> getCurrencies();

  /**
   * Computes the sensitivity to the parameters of the given currency discounting curve from the sensitivity to yield (continuously compounded) at intermediary points.
   * @param ccy The currency.
   * @param pointSensitivity The point yield sensitivity.
   * @return The parameters sensitivity.
   */
  double[] parameterSensitivity(Currency ccy, List<DoublesPair> pointSensitivity);

  /**
   * Returns the number of parameters associated to a currency.
   * @param ccy The currency.
   * @return The number of parameters.
   */
  int getNumberOfParameters(Currency ccy);

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
   * Returns the curve name associated to an index.
   * @param index The index.
   * @return The name.
   */
  String getName(IborIndex index);

  /**
   * Returns a set of all the index available for forward.
   * @return The index set.
   */
  Set<IborIndex> getIndexesIbor();

  /**
   * Computes the sensitivity to the parameters of the given index forward curve from the sensitivity to forward rate at intermediary points.
   * @param index The index.
   * @param pointSensitivity The point yield sensitivity.
   * @return The parameters sensitivity.
   */
  double[] parameterSensitivity(IborIndex index, List<MarketForwardSensitivity> pointSensitivity);

  /**
   * Returns the number of parameters associated to an index.
   * @param index The index.
   * @return The number of parameters.
   */
  int getNumberOfParameters(IborIndex index);

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  double getForwardRate(IndexON index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @return The forward rate.
   */
  double getForwardRate(IndexON index, double startTime);

  /**
   * Returns the curve name associated to an index.
   * @param index The index.
   * @return The name.
   */
  String getName(IndexON index);

  /**
   * Returns a set of all the index available for forward.
   * @return The index set.
   */
  Set<IndexON> getIndexesON();

  /**
   * Computes the sensitivity to the parameters of the given index forward curve from the sensitivity to forward rate at intermediary points.
   * @param index The index.
   * @param pointSensitivity The point yield sensitivity.
   * @return The parameters sensitivity.
   */
  double[] parameterSensitivity(IndexON index, List<MarketForwardSensitivity> pointSensitivity);

  /**
   * Returns the number of parameters associated to an index.
   * @param index The index.
   * @return The number of parameters.
   */
  int getNumberOfParameters(IndexON index);

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

  /**
   * Gets the set of price indexes defined in the market.
   * @return The set of index.
   */
  Set<IndexPrice> getPriceIndexes();

  /**
   * Gets the set of issuer names by currency defined in the market.
   * @return The set of issuers names/currencies.
   */
  Set<Pair<String, Currency>> getIssuersCcy();

  /**
   * Gets the names of all curves (discounting, forward, price index and issuers).
   * @return The names.
   */
  Set<String> getAllNames();

  /**
   * Return the exchange rate between two currencies.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  double getFxRate(final Currency ccy1, final Currency ccy2);

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  FXMatrix getFxRates();

}
