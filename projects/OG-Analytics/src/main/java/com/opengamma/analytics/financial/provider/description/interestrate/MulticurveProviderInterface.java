/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Interface of a multi-curves framework providing discounting factors, forward rate (linked to Ibor index), issuer/currency specific curves and currency exchange rates.
 */
public interface MulticurveProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The new provider.
   */
  @Override
  MulticurveProviderInterface copy();

  /**
   * Gets the discount factor for one currency at a given time to maturity.
   * @param ccy The currency.
   * @param time The time.
   * @return The discount factor.
   */
  // TODO: extend it to a more general unique reference to include issuer/currency curves? UniqueIdentifiable?
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
  // TODO: Do we want to have a unique method for IborIndex and IndexON? UniqueIdentifiable?
  double getForwardRate(IndexON index, double startTime, double endTime, double accrualFactor);

  /**
   * Return the exchange rate between two currencies.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  double getFxRate(final Currency ccy1, final Currency ccy2);

  // TODO: Maybe some of the methods below should be in an implementation class.
  // REVIEW emcleod 2013-9-16 Yes, they should be moved - these classes do far too much and there's
  // quite a lot of code repeated between various providers.
  /**
   * Gets the sensitivities to the curve parameters.
   * @param name The curve name
   * @param pointSensitivity The point sensitivities
   * @return The sensitivities to the parameters
   */
  double[] parameterSensitivity(String name, List<DoublesPair> pointSensitivity);

  /**
   * Gets the forward sensitivities to the curve parameters.
   * @param name The curve name
   * @param pointSensitivity The point sensitivities
   * @return The forward sensitivities to the parameters
   */
  double[] parameterForwardSensitivity(String name, List<ForwardSensitivity> pointSensitivity);

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

  //     =====     Related methods for the discounting curves     =====

  /**
   * Return the name associated to the discounting in a currency.
   * @param ccy The currency.
   * @return The name.
   */
  String getName(Currency ccy);

  // TODO: Replace the curve names by some curve ID, maybe some UniqueIdentifiable objects
  // TODO: Some method could be available from curve ID and not only from financial description (like parameterSensitivity(CurveID id, List<DoublesPair> pointSensitivity))

  /**
   * Returns a set of all the currencies available for discounting.
   * @return The currency set.
   */
  Set<Currency> getCurrencies();

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

}
