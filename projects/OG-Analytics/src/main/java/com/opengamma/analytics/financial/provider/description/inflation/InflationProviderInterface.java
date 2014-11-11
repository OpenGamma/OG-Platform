/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * Interface specific to inflation curves.
 * Compose the MulticurveProviderInterface.
 */
public interface InflationProviderInterface extends ParameterInflationProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The provider.
   */
  @Override
  InflationProviderInterface copy();

  /**
   * Gets the estimated price index for a given reference time.
   * @param index The price index.
   * @param time The reference time.
   * @return The price index.
   */
  double getPriceIndex(IndexPrice index, Double time);

  // TODO: Do we need a method which returns the inflation rate over a period?

  /**
   * Return the name of the curve associated to a price index.
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
   * Returns an unmodifiable sorted set containing the names of all curves (discounting, forward, price index and issuers).
   * @return The names.
   * @deprecated Use {@link #getAllCurveNames()}
   */
  @Deprecated
  Set<String> getAllNames();

  /**
   * Gets the number of parameters for a curve described by its name.
   * @param name The curve name.
   * @return The number of parameters.
   */
  Integer getNumberOfParameters(String name);

  /**
   * Gets the underlying name(s) (i.e. {@link YieldAndDiscountCurve#getName()} for a curve name;
   * this can be multi-valued in the case of spread curves.
   * @param name The curve name
   * @return The name(s) of the underlying curves.
   */
  List<String> getUnderlyingCurvesNames(String name);

  /**
   * Returns the MulticurveProvider from which the InflationProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  MulticurveProviderInterface getMulticurveProvider();

  //     =====     Methods related to MulticurveProvider     =====
  // TODO: not required? The getMulticurveProvider is enough.

  /**
   * Gets the discount factor for one currency at a given time to maturity.
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
   * Returns a new provider with the discounting curve for a particular currency replaced.
   * @param ccy The currency, not null
   * @param replacement The replacement discounting curve, not null
   * @return A new provider with the discounting curve for the currency replaced by the input curve.
   */
  InflationProviderInterface withDiscountFactor(Currency ccy, YieldAndDiscountCurve replacement);

  /**
   * Returns a new provider with the curve for a particular ibor index replaced.
   * @param index The ibor index, not null
   * @param replacement The replacement ibor index curve, not null
   * @return A new provider with the ibor index curve replaced by the input curve.
   */
  InflationProviderInterface withForward(IborIndex index, YieldAndDiscountCurve replacement);

  /**
   * Returns a new provider with the curve for a particular overnight index replaced.
   * @param index The overnight index, not null
   * @param replacement The replacement overnight index curve, not null
   * @return A new provider with the overnight index curve replaced by the input curve.
   */
  InflationProviderInterface withForward(IndexON index, YieldAndDiscountCurve replacement);

}
