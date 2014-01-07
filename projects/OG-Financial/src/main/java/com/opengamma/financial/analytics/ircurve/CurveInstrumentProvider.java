/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Provides the market data {@link ExternalId} of a curve instrument given information about the node.
 */
public interface CurveInstrumentProvider {

  /**
   * Gets the external id of the market data given a tenor and curve construction date.
   * @param curveDate The curve construction date
   * @param tenor The tenor of the node
   * @return The external id for the market data of the instrument
   */
  ExternalId getInstrument(LocalDate curveDate, Tenor tenor);

  /**
   * Gets the external id of the market data for a future given a curve construction date,
   * start tenor, future tenor and the number of future tenors from the start tenor.
   * @param curveDate The curve construction date
   * @param startTenor The tenor from which to start counting futures
   * @param futureTenor The tenor of the futures
   * @param numFutureFromTenor The number of future tenors to use
   * @return The external id for the market data of the instrument
   */
  ExternalId getInstrument(LocalDate curveDate, Tenor startTenor, Tenor futureTenor, int numFutureFromTenor);

  /**
   * Gets the external id of the market data for an IMM instrument given a curve construction date,
   * start tenor, start IMM period number and end IMM period number.
   * @param curveDate The curve construction date
   * @param startTenor The tenor from which to start counting IMM periods
   * @param startIMMPeriods The number of IMM periods to the start of the swap from the start tenor
   * @param endIMMPeriods The number of IMM periods to the end of the swap from the start tenor
   * @return The external id for the market data of the instrument
   */
  ExternalId getInstrument(LocalDate curveDate, Tenor startTenor, int startIMMPeriods, int endIMMPeriods);

  /**
   * Gets the market data field to use for this identifier.
   * @return The market data field
   */
  String getMarketDataField();

  /**
   * Gets the type of the market data field
   * @return The data field type
   */
  DataFieldType getDataFieldType();

  /**
   * Gets the external id of the market data for a <b>quarterly</b> future given a
   * curve construction date, start tenor and the number future tenors from the start tenor.
   * @param curveDate The curve construction date
   * @param tenor The tenor of the node
   * @param numQuarterlyFuturesFromTenor The number of the future
   * @return The external id for the market data of the instrument
   * @deprecated Use the version that does not assume that all futures are quarterly.
   */
  @Deprecated
  ExternalId getInstrument(LocalDate curveDate, Tenor tenor, int numQuarterlyFuturesFromTenor);

  /**
   * Gets the external id of the market data for a periodic zero deposit strip given a curve
   * construction date and tenor.
   * @param curveDate The curve construction date
   * @param tenor The tenor of the node
   * @param periodsPerYear The number of periods in a year
   * @param isPeriodicZeroDeposit Is this instrument a periodic zero deposit node.
   * @return The external id for the market data of the instrument
   * @deprecated This method should only be used for strips of type {@link StripInstrumentType#PERIODIC_ZERO_DEPOSIT}
   */
  @Deprecated
  ExternalId getInstrument(LocalDate curveDate, Tenor tenor, int periodsPerYear, boolean isPeriodicZeroDeposit);

  /**
   * Gets the external id of the market data for a basis swap strip given a curve construction date,
   * tenor and information about the pay and receive floating leg tenors and index type.
   * @param curveDate The curve construction date
   * @param tenor The tenor of the node
   * @param payTenor The pay tenor of the basis swap
   * @param receiveTenor The receive tenor of the basis swap
   * @param payIndexType The pay floating index type
   * @param receiveIndexType The receive floating index type
   * @return The external id for the market data of the instrument
   * @deprecated This method should only be used for strips of type {@link StripInstrumentType#BASIS_SWAP}
   * or {@link StripInstrumentType#TENOR_SWAP}
   */
  @Deprecated
  ExternalId getInstrument(LocalDate curveDate, Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType, final IndexType receiveIndexType);

  /**
   * Gets the external id of the market data for a fixed / float swap strip given a curve construction date,
   * tenor and information about the floating rate reset tenor and index type.
   * @param curveDate The curve construction date
   * @param tenor The tenor of the node
   * @param resetTenor The reset tenor of the floating rate
   * @param indexType The floating index type
   * @return The external id for the market data of the instrument
   * @deprecated This method should only be used for strips of type {@link StripInstrumentType#SWAP}
   */
  @Deprecated
  ExternalId getInstrument(LocalDate curveDate, Tenor tenor, final Tenor resetTenor, final IndexType indexType);
}
