/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.ForwardSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Interface of a multi-curves framework providing discounting factors, forward rate (linked to Ibor index), issuer/currency specific curves and currency exchange rates.
 */
public interface MulticurveProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  MulticurveProviderInterface copy();

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

  // TODO: Probably the methods below should be in an implementation class.
  //     =====     Related methods for the discounting curves     =====

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

  //     =====     Related methods for the forward curves Ibor    =====

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
  double[] parameterSensitivity(IborIndex index, List<ForwardSensitivity> pointSensitivity);

  /**
   * Returns the number of parameters associated to an index.
   * @param index The index.
   * @return The number of parameters.
   */
  int getNumberOfParameters(IborIndex index);

  //     =====     Related methods for the forward ON curves     =====

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
  double[] parameterSensitivity(IndexON index, List<ForwardSensitivity> pointSensitivity);

  /**
   * Returns the number of parameters associated to an index.
   * @param index The index.
   * @return The number of parameters.
   */
  int getNumberOfParameters(IndexON index);

  //     =====     Related methods FX     =====

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  FXMatrix getFxRates();

  /**
   * Gets the names of all curves (discounting, forward, price index and issuers).
   * @return The names.
   */
  Set<String> getAllNames();

  /**
   * Gets the number of parameters for a curve described by its name.
   * @param name The curve name.
   * @return The number of parameters.
   */
  Integer getNumberOfParameters(String name);

  //     =====     Convenience methods     =====

  MulticurveProviderInterface withDiscountFactor(Currency ccy, YieldAndDiscountCurve replacement);

}
