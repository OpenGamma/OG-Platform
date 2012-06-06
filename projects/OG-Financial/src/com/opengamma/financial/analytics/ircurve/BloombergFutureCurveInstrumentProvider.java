/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.BloombergIRFutureUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class BloombergFutureCurveInstrumentProvider implements CurveInstrumentProvider {

  private static BiMap<MonthOfYear, Character> s_monthCode;
  private final String _futurePrefix;
  private final String _marketSector;

  static {
    s_monthCode = HashBiMap.create();
    s_monthCode.put(MonthOfYear.JANUARY, 'F');
    s_monthCode.put(MonthOfYear.FEBRUARY, 'G');
    s_monthCode.put(MonthOfYear.MARCH, 'H');
    s_monthCode.put(MonthOfYear.APRIL, 'J');
    s_monthCode.put(MonthOfYear.MAY, 'K');
    s_monthCode.put(MonthOfYear.JUNE, 'M');
    s_monthCode.put(MonthOfYear.JULY, 'N');
    s_monthCode.put(MonthOfYear.AUGUST, 'Q');
    s_monthCode.put(MonthOfYear.SEPTEMBER, 'U');
    s_monthCode.put(MonthOfYear.OCTOBER, 'V');
    s_monthCode.put(MonthOfYear.NOVEMBER, 'X');
    s_monthCode.put(MonthOfYear.DECEMBER, 'Z');
  }

  public BloombergFutureCurveInstrumentProvider(final String futurePrefix, final String marketSector) {
    _futurePrefix = futurePrefix;
    _marketSector = marketSector;
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
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    throw new OpenGammaRuntimeException("Only futures supported");
  }

  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER;

  private ExternalId createQuarterlyIRFutureStrips(final LocalDate curveDate, final Tenor tenor, final int numQuartlyFuturesFromTenor, final String prefix, final String postfix) {
    final StringBuilder futureCode = new StringBuilder();
    futureCode.append(prefix);
    final LocalDate curveFutureStartDate = curveDate.plus(tenor.getPeriod());
    final String expiryCode = BloombergIRFutureUtils.getQuarterlyExpiryCodeForFutures(prefix, numQuartlyFuturesFromTenor, curveFutureStartDate);
    futureCode.append(expiryCode);
    futureCode.append(postfix);
    return ExternalId.of(SCHEME, futureCode.toString());
  }

  // for serialisation only
  public String getFuturePrefix() {
    return _futurePrefix;
  }

  // for serialisation only
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
    return getFuturePrefix().equals(other.getFuturePrefix()) && getMarketSector().equals(other.getMarketSector());
  }

  @Override
  public int hashCode() {
    return getFuturePrefix().hashCode() ^ getMarketSector().hashCode() * (2 ^ 16);
  }
}
