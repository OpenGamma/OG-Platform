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
import com.opengamma.util.money.Currency;

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
   * Gets the investment factor for one Ibor index between start and end times.
   * This quantity correspond to growth between the start and the end time for an investment of 1 unit, assuming the investment growth according to the underlying curve
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  double getInvestmentFactor(IborIndex index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  double getSimplyCompoundForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the forward for one Ibor index between start and end times. The accrual factor is computed with the start and the end time (end time -start time).
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @return The forward rate.
   */
  double getSimplyCompoundForwardRate(IborIndex index, double startTime, double endTime);

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  double getAnnuallyCompoundForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the forward for one Ibor index between start and end times. The accrual factor is computed with the start and the end time (end time -start time).
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @return The forward rate.
   */
  double getAnnuallyCompoundForwardRate(IborIndex index, double startTime, double endTime);

  /**
   * Gets the investment factor for one Ibor index between start and end times.
   * This quantity correspond to growth between the start and the end time for an investment of 1 unit, assuming the investment growth according to the underlying curve
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  double getInvestmentFactor(IndexON index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the forward for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  // TODO: Do we want to have a unique method for IborIndex and IndexON? UniqueIdentifiable?
  double getSimplyCompoundForwardRate(IndexON index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the forward for one Ibor index between start and end times. The accrual factor is computed with the start and the end time (end time -start time).
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @return The forward rate.
   */
  // TODO: Do we want to have a unique method for IborIndex and IndexON? UniqueIdentifiable?
  double getSimplyCompoundForwardRate(IndexON index, double startTime, double endTime);

  /**
   * Gets the annual compound forward ( it corresponds to $\frac{DiscountFactor(t_1)}{DiscountFactor(t_1)}^(1/accrualFactor)-1$)for one Ibor index between start and end times.
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param accrualFactor The Ibor accrual factor.
   * @return The forward rate.
   */
  // TODO: Do we want to have a unique method for IborIndex and IndexON? UniqueIdentifiable?
  double getAnnuallyCompoundForwardRate(IndexON index, double startTime, double endTime, double accrualFactor);

  /**
   * Gets the annual compound forward for one Ibor index between start and end times. The accrual factor is computed with the start and the end time (end time -start time).
   * @param index The Ibor index.
   * @param startTime The start time.
   * @param endTime The end time.
   * @return The forward rate.
   */
  // TODO: Do we want to have a unique method for IborIndex and IndexON? UniqueIdentifiable?
  double getAnnuallyCompoundForwardRate(IndexON index, double startTime, double endTime);

  /**
   * Return the exchange rate between two currencies.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  double getFxRate(final Currency ccy1, final Currency ccy2);

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
