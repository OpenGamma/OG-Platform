/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.volatility.surface.BloombergFutureUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Provides market data ids for quarterly or monthly futures.
 */
public class BloombergFutureCurveInstrumentProvider implements CurveInstrumentProvider {
  /** The external scheme for the tickers */
  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER;
  /** Bloomberg month codes for futures */
  private static BiMap<Month, Character> s_monthCode;
  /** The future prefix */
  private final String _futurePrefix;
  /** The market sector postfix */
  private final String _marketSector;
  /** The market data field */
  private final String _dataField;
  /** The data field type */
  private final DataFieldType _fieldType;

  static {
    s_monthCode = HashBiMap.create();
    s_monthCode.put(Month.JANUARY, 'F');
    s_monthCode.put(Month.FEBRUARY, 'G');
    s_monthCode.put(Month.MARCH, 'H');
    s_monthCode.put(Month.APRIL, 'J');
    s_monthCode.put(Month.MAY, 'K');
    s_monthCode.put(Month.JUNE, 'M');
    s_monthCode.put(Month.JULY, 'N');
    s_monthCode.put(Month.AUGUST, 'Q');
    s_monthCode.put(Month.SEPTEMBER, 'U');
    s_monthCode.put(Month.OCTOBER, 'V');
    s_monthCode.put(Month.NOVEMBER, 'X');
    s_monthCode.put(Month.DECEMBER, 'Z');
  }

  /**
   * Sets the market data field to {@link MarketDataRequirementNames#MARKET_VALUE}
   * @param futurePrefix The future prefix, not null
   * @param marketSector The market sector postfix, not null
   */
  public BloombergFutureCurveInstrumentProvider(final String futurePrefix, final String marketSector) {
    this(futurePrefix, marketSector, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT);
  }

  /**
   * @param futurePrefix The future prefix, not null
   * @param marketSector The market sector postfix, not null
   * @param dataField The market data field, not null
   * @param fieldType The data field type, not null
   */
  public BloombergFutureCurveInstrumentProvider(final String futurePrefix, final String marketSector,
      final String dataField, final DataFieldType fieldType) {
    ArgumentChecker.notNull(futurePrefix, "future prefix");
    ArgumentChecker.notNull(marketSector, "market sector");
    ArgumentChecker.notNull(dataField, "data field");
    ArgumentChecker.notNull(fieldType, "field type");
    _futurePrefix = futurePrefix;
    _marketSector = marketSector;
    _dataField = dataField;
    _fieldType = fieldType;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    return createQuarterlyIRFutureStrips(curveDate, tenor, numQuarterlyFuturesFromTenor, _futurePrefix, " " + _marketSector);
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFuturesFromTenor) {
    //TODO there must be a more elegant way to do this
    if (futureTenor.equals(Tenor.THREE_MONTHS)) {
      return createQuarterlyIRFutureStrips(curveDate, startTenor, numFuturesFromTenor, _futurePrefix, " " + _marketSector);
    } else if (futureTenor.equals(Tenor.ONE_MONTH)) {
      return createMonthlyIRFutureStrips(curveDate, startTenor, numFuturesFromTenor, _futurePrefix, " " + _marketSector);
    }
    throw new OpenGammaRuntimeException("Can only create ids for quarterly or monthly tenors");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final int startIMMPeriods, final int endIMMPeriods) {
    throw new UnsupportedOperationException("Only futures supported");
  }

  @Override
  public String getMarketDataField() {
    return _dataField;
  }

  @Override
  public DataFieldType getDataFieldType() {
    return _fieldType;
  }
  private ExternalId createQuarterlyIRFutureStrips(final LocalDate curveDate, final Tenor tenor, final int numQuartlyFuturesFromTenor, final String prefix, final String postfix) {
    final StringBuilder futureCode = new StringBuilder();
    futureCode.append(prefix);
    final LocalDate curveFutureStartDate = curveDate.plus(tenor.getPeriod());
    final String expiryCode = BloombergFutureUtils.getQuarterlyExpiryCodeForFutures(prefix, numQuartlyFuturesFromTenor, curveFutureStartDate);
    futureCode.append(expiryCode);
    futureCode.append(postfix);
    return ExternalId.of(SCHEME, futureCode.toString());
  }

  private ExternalId createMonthlyIRFutureStrips(final LocalDate curveDate, final Tenor tenor, final int numMonthlyFuturesFromTenor, final String prefix, final String postfix) {
    final StringBuilder futureCode = new StringBuilder();
    futureCode.append(prefix);
    final LocalDate curveFutureStartDate = curveDate.plus(tenor.getPeriod());
    final String expiryCode = BloombergFutureUtils.getMonthlyExpiryCodeForFutures(prefix, numMonthlyFuturesFromTenor, curveFutureStartDate);
    futureCode.append(expiryCode);
    futureCode.append(postfix);
    return ExternalId.of(SCHEME, futureCode.toString());
  }

  /**
   * Gets the future prefix.
   * @return The future prefix
   */
  public String getFuturePrefix() {
    return _futurePrefix;
  }

  /**
   * Gets the market sector postfix.
   * @return The market sector postfix
   */
  public String getMarketSector() {
    return _marketSector;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof BloombergFutureCurveInstrumentProvider)) {
      return false;
    }
    final BloombergFutureCurveInstrumentProvider other = (BloombergFutureCurveInstrumentProvider) o;
    return getFuturePrefix().equals(other.getFuturePrefix()) &&
        getMarketSector().equals(other.getMarketSector()) &&
        getMarketDataField().equals(other.getMarketDataField()) &&
        getDataFieldType() == other.getDataFieldType();
  }

  @Override
  public int hashCode() {
    return getFuturePrefix().hashCode() ^ getMarketSector().hashCode() ^ getMarketDataField().hashCode()
        ^ getDataFieldType().hashCode() * (2 ^ 16);
  }
}
