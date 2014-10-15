/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.volatility.surface.BloombergFutureUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Provides market data ids for quarterly or monthly futures.
 * This is similar to {@code BloombergFutureCurveInstrumentProvider} except no space is added between the ticker and the
 * sector. It can be used with any scheme that uses the same month delivery codes as Bloomberg.
 */
public class FutureMonthCodeCurveInstrumentProvider implements CurveInstrumentProvider {
  /** Month codes for futures */
  private static BiMap<Month, Character> s_monthCode;
  /** The future prefix */
  private final String _futurePrefix;
  /** The future suffix */
  private final String _futureSuffix;
  /** The market data field */
  private final String _dataField;
  /** The data field type */
  private final DataFieldType _fieldType;
  /** The ticker scheme */
  private final ExternalScheme _scheme;

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
   *
   * @param futurePrefix The future prefix, not null
   * @param futureSuffix The future postfix, not null. Empty string for no ticker postfix, if a space is required
   * between the month code and the sector e.g. for Bloomberg "Z4 Comdty", the sector would be " Comdty" (with a leading
   * space). Note this is different from the BloombergFutureCurveInstrumentProvider which adds the space automatically.
   * @param scheme The ticker scheme, not null
   */
  public FutureMonthCodeCurveInstrumentProvider(final String futurePrefix, final String futureSuffix,
                                                final ExternalScheme scheme) {
    this(futurePrefix, futureSuffix, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT, scheme);
  }

  /**
   * @param futurePrefix The future prefix, not null
   * @param futureSuffix The market sector postfix, not null. Empty string for no ticker postfix, if a space is required
   * between the month code and the sector e.g. for Bloomberg "Z4 Comdty", the sector would be " Comdty" (with a leading
   * space). Note this is different from the BloombergFutureCurveInstrumentProvider which adds the space automatically.
   * @param dataField The market data field, not null
   * @param fieldType The data field type, not null
   * @param scheme The ticker scheme, not null
   */
  public FutureMonthCodeCurveInstrumentProvider(final String futurePrefix, final String futureSuffix,
                                                final String dataField, final DataFieldType fieldType,
                                                final ExternalScheme scheme) {
    ArgumentChecker.notNull(futurePrefix, "future prefix");
    ArgumentChecker.notNull(futureSuffix, "market sector");
    ArgumentChecker.notNull(dataField, "data field");
    ArgumentChecker.notNull(fieldType, "field type");
    ArgumentChecker.notNull(scheme, "scheme");
    _futurePrefix = futurePrefix;
    _futureSuffix = futureSuffix;
    _dataField = dataField;
    _fieldType = fieldType;
    _scheme = scheme;
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
    return createQuarterlyIRFutureStrips(curveDate, tenor, numQuarterlyFuturesFromTenor, _futurePrefix, _futureSuffix);
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFuturesFromTenor) {
    //TODO there must be a more elegant way to do this
    if (futureTenor.equals(Tenor.THREE_MONTHS)) {
      return createQuarterlyIRFutureStrips(curveDate, startTenor, numFuturesFromTenor, _futurePrefix, _futureSuffix);
    } else if (futureTenor.equals(Tenor.ONE_MONTH)) {
      return createMonthlyIRFutureStrips(curveDate, startTenor, numFuturesFromTenor, _futurePrefix, _futureSuffix);
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
    return ExternalId.of(_scheme, futureCode.toString());
  }

  private ExternalId createMonthlyIRFutureStrips(final LocalDate curveDate, final Tenor tenor, final int numMonthlyFuturesFromTenor, final String prefix, final String postfix) {
    final StringBuilder futureCode = new StringBuilder();
    futureCode.append(prefix);
    final LocalDate curveFutureStartDate = curveDate.plus(tenor.getPeriod());
    final String expiryCode = BloombergFutureUtils.getMonthlyExpiryCodeForFutures(prefix, numMonthlyFuturesFromTenor, curveFutureStartDate);
    futureCode.append(expiryCode);
    futureCode.append(postfix);
    return ExternalId.of(_scheme, futureCode.toString());
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
  public String getFutureSuffix() {
    return _futureSuffix;
  }

  /**
   * Gets the ticker scheme
   * @return The ticker scheme
   */
  public ExternalScheme getScheme() {
    return _scheme;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof FutureMonthCodeCurveInstrumentProvider)) {
      return false;
    }
    final FutureMonthCodeCurveInstrumentProvider other = (FutureMonthCodeCurveInstrumentProvider) o;
    return getFuturePrefix().equals(other.getFuturePrefix()) &&
        getFutureSuffix().equals(other.getFutureSuffix()) &&
        getMarketDataField().equals(other.getMarketDataField()) &&
        getDataFieldType() == other.getDataFieldType() &&
        getScheme() == other.getScheme();
  }

  @Override
  public int hashCode() {
    return getFuturePrefix().hashCode() ^ getFutureSuffix().hashCode() ^ getMarketDataField().hashCode()
        ^ getDataFieldType().hashCode() ^ getScheme().hashCode() * (2 ^ 16);
  }
}
