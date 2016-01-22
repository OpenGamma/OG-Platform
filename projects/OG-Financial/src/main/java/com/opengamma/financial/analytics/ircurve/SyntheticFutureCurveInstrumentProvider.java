/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.model.irfutureoption.FutureOptionUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Provides market data ids for synthetic futures.
 */
public class SyntheticFutureCurveInstrumentProvider implements CurveInstrumentProvider, Serializable {
  /** The ticker scheme */
  private static final ExternalScheme SCHEME = ExternalSchemes.OG_SYNTHETIC_TICKER;
  /** The month codes */
  private static final BiMap<Month, Character> MONTH_CODE;
  /** The future prefix */
  private final String _futurePrefix;

  static {
    MONTH_CODE = HashBiMap.create();
    MONTH_CODE.put(Month.JANUARY, 'F');
    MONTH_CODE.put(Month.FEBRUARY, 'G');
    MONTH_CODE.put(Month.MARCH, 'H');
    MONTH_CODE.put(Month.APRIL, 'J');
    MONTH_CODE.put(Month.MAY, 'K');
    MONTH_CODE.put(Month.JUNE, 'M');
    MONTH_CODE.put(Month.JULY, 'N');
    MONTH_CODE.put(Month.AUGUST, 'Q');
    MONTH_CODE.put(Month.SEPTEMBER, 'U');
    MONTH_CODE.put(Month.OCTOBER, 'V');
    MONTH_CODE.put(Month.NOVEMBER, 'X');
    MONTH_CODE.put(Month.DECEMBER, 'Z');
  }

  /**
   * @param futurePrefix The future prefix, not null
   */
  public SyntheticFutureCurveInstrumentProvider(final String futurePrefix) {
    ArgumentChecker.notNull(futurePrefix, "future prefix");
    _futurePrefix = futurePrefix;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFutureFromTenor) {
    if (futureTenor.equals(Tenor.THREE_MONTHS)) {
      return createQuarterlyIRFutureStrips(curveDate, startTenor, numFutureFromTenor, _futurePrefix);
    } else if (futureTenor.equals(Tenor.ONE_MONTH)) {
      return createMonthlyIRFutureStrips(curveDate, startTenor, numFutureFromTenor, _futurePrefix);
    }
    throw new OpenGammaRuntimeException("Can only create ids for quarterly or monthly tenors");
  }

  /**
   * Gets the future prefix.
   * @return The future prefix
   */
  public String getFuturePrefix() {
    return _futurePrefix;
  }

  @Override
  public String getMarketDataField() {
    return MarketDataRequirementNames.MARKET_VALUE;
  }

  @Override
  public DataFieldType getDataFieldType() {
    return DataFieldType.OUTRIGHT;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    return createQuarterlyIRFutureStrips(curveDate, tenor, numQuarterlyFuturesFromTenor, _futurePrefix);
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType, final IndexType receiveIndexType) {
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

  private static ExternalId createQuarterlyIRFutureStrips(final LocalDate curveDate, final Tenor tenor, final int numQuartlyFuturesFromTenor, final String prefix) {
    final StringBuilder futureCode = new StringBuilder();
    futureCode.append(prefix);
    final LocalDate curveFutureStartDate = curveDate.plus(tenor.getPeriod());
    final String expiryCode = getQuarterlyExpiryCodeForFutures(numQuartlyFuturesFromTenor, curveFutureStartDate);
    futureCode.append(expiryCode);
    return ExternalId.of(SCHEME, futureCode.toString());
  }

  private static ExternalId createMonthlyIRFutureStrips(final LocalDate curveDate, final Tenor tenor, final int numMonthlyFuturesFromTenor, final String prefix) {
    final StringBuilder futureCode = new StringBuilder();
    futureCode.append(prefix);
    final LocalDate curveFutureStartDate = curveDate.plus(tenor.getPeriod());
    final String expiryCode = getMonthlyExpiryCodeForFutures(numMonthlyFuturesFromTenor, curveFutureStartDate);
    futureCode.append(expiryCode);
    return ExternalId.of(SCHEME, futureCode.toString());
  }

  private static String getQuarterlyExpiryCodeForFutures(final int nthFuture, final LocalDate curveDate) {
    final LocalDate expiry = FutureOptionUtils.getApproximateIRFutureQuarterlyExpiry(nthFuture, curveDate);
    final StringBuilder result = new StringBuilder(MONTH_CODE.get(expiry.getMonth()).toString());
    result.append(expiry.getYear() % 1000);
    return result.toString();
  }

  private static String getMonthlyExpiryCodeForFutures(final int nthFuture, final LocalDate curveDate) {
    final LocalDate expiry = FutureOptionUtils.getApproximateIRFutureMonth(nthFuture, curveDate);
    final StringBuilder result = new StringBuilder(MONTH_CODE.get(expiry.getMonth()));
    result.append(expiry.getYear() % 1000);
    return result.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _futurePrefix.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SyntheticFutureCurveInstrumentProvider other = (SyntheticFutureCurveInstrumentProvider) obj;
    return ObjectUtils.equals(_futurePrefix, other._futurePrefix);
  }

}
