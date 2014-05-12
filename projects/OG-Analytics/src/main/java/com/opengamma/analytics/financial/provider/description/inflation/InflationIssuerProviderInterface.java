/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface specific to inflation curves.
 * Compose the MulticurveProviderInterface.
 */
public interface InflationIssuerProviderInterface extends ParameterIssuerProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  InflationIssuerProviderInterface copy();

  /**
   * Gets the estimated price index for a given reference time.
   * @param index The price index.
   * @param time The reference time.
   * @return The price index.
   */
  double getPriceIndex(IndexPrice index, Double time);

  // TODO: Do we need a method which returns the inflation rate over a period?

  /**
   * Return the name associated to a price index.
   * @param index The price index.
   * @return The name.
   */
  String getName(IndexPrice index);

  /**
   * Gets the set of price indexes defined in the provider.
   * @return The set of index.
   */
  Set<IndexPrice> getPriceIndexes();

  /**
   * Gets the discount factor for one issuer in one currency.
   * @param issuerCcy The issuer name/currency pair.
   * @param time The time.
   * @return The discount factor.
   */
  double getDiscountFactor(Pair<Object, LegalEntityFilter<LegalEntity>> issuerCcy, Double time);

  /**
   * Gets the set of issuer names by currency defined in the market.
   * @return The set of issuers names/currencies.
   */
  Set<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers();

  /**
   * Gets the names of all curves (discounting, forward, price index and issuers).
   * @return The names.
   * @deprecated Use {@link #getAllNames()}
   */
  @Deprecated
  Set<String> getAllNames();

  /**
   * Gets an unmodifiable sorted set of the names of all curves. An empty set of is returned
   * if there are no curves in this provider.
   * @return The names.
   */
  @Override
  Set<String> getAllCurveNames();

  /**
   * Returns the MulticurveProvider from which the InflationProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  MulticurveProviderInterface getMulticurveProvider();

  /**
   * Returns the InflationProvider from which the InflationIssuerProvider is composed.
   * @return The inflation provider.
   */
  InflationProviderInterface getInflationProvider();

  //     =====     Methods related to MulticurveProvider     =====

  /**
   * Gets the discount factor for one currency at a given time to maturity.
   * TODO: extend it to a more general unique reference to include issuer/currency curves? UniqueIdentifiable?
   * @param ccy The currency.
   * @param time The time.
   * @return The discount factor.
   */
  double getDiscountFactor(Currency ccy, Double time);

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
   * TODO: Do we want to have a unique method for IborIndex and IndexON? UniqueIdentifiable?
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  double getForwardRate(IndexON index, double startTime, double endTime, double accrualFactor);

  /**
   * Return the exchange rate between two currencies.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  double getFxRate(final Currency ccy1, final Currency ccy2);

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
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  FXMatrix getFxRates();

  //     =====     Convenience methods     =====

  /**
   * Replaces the identifier / issuer pair for a particular currency.
   * @param ccy The currency
   * @param replacement The replacement identifier / issuer pair
   * @return A new provider with the appropriate pair replaced
   */
  InflationProviderInterface withDiscountFactor(Currency ccy, Pair<Object, LegalEntityFilter<LegalEntity>> replacement);

  /**
   * Replaces an issuer for a particular currency.
   * @param ccy The currency The currency
   * @param replacement The replacement issuer
   * @return A new provider with the appropriate issuer replaced
   */
  InflationProviderInterface withDiscountFactor(Currency ccy, LegalEntity replacement);
}
