/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.commodity;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Interface specific to commodity curves.
 * Compose the MulticurveProviderInterface.
 */
public interface CommodityProviderInterface extends ParameterCommodityProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The provider.
   */
  @Override
  CommodityProviderInterface copy();

  /**
   * Gets the estimated commodity forward value for a given reference time.
   * @param underlying The commodity underlying.
   * @param time The reference time.
   * @return The forward value.
   */
  double getForwardValue(CommodityUnderlying underlying, Double time);

  /**
   * Return the name associated to a commodity underlying.
   * @param underlying The underlying commodity.
   * @return The name.
   */
  String getName(CommodityUnderlying underlying);

  /**
   * Gets the set of price indexes defined in the provider.
   * @return The set of index.
   */
  Set<CommodityUnderlying> getCommodityUnderlyings();

  /**
   * Returns an unmodifiable sorted set of the names of all curves (discounting, forward, price index and issuers).
   * If there are no curves, an empty set is returned.
   * @return The names.
   * @deprecated Use {@link #getAllCurveNames()}
   */
  @Deprecated
  Set<String> getAllNames();

  /**
   * Gets the sensitivity to the commodity parameters.
   * @param name The name of the curve
   * @param pointSensitivity The nodal point sensitivities
   * @return The sensitivity to the inflation parameters
   */
  double[] parameterCommoditySensitivity(String name, List<DoublesPair> pointSensitivity);

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
   * Returns a new provider with the discounting curve for a particular currency replaced.
   * @param ccy The currency, not null
   * @param replacement The replacement discounting curve, not null
   * @return A new provider with the discounting curve for the currency replaced by the input curve.
   */
  CommodityProviderInterface withDiscountFactor(Currency ccy, YieldAndDiscountCurve replacement);

  /**
   * Returns a new provider with the curve for a particular ibor index replaced.
   * @param index The ibor index, not null
   * @param replacement The replacement ibor index curve, not null
   * @return A new provider with the ibor index curve replaced by the input curve.
   */
  CommodityProviderInterface withForward(IborIndex index, YieldAndDiscountCurve replacement);

  /**
   * Returns a new provider with the curve for a particular overnight index replaced.
   * @param index The overnight index, not null
   * @param replacement The replacement overnight index curve, not null
   * @return A new provider with the overnight index curve replaced by the input curve.
   */
  CommodityProviderInterface withForward(IndexON index, YieldAndDiscountCurve replacement);

}
