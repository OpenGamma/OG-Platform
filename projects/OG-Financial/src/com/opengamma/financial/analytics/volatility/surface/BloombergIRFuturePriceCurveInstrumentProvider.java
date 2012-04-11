/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.text.DecimalFormat;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 *  Provider of Interest Rate Future Instrument ID's.
 *  Tied closely to BloombergIRFutureInstrumentProviderUtils.
 */
public class BloombergIRFuturePriceCurveInstrumentProvider implements FuturePriceCurveInstrumentProvider<Number> {
  
  private static final DecimalFormat FORMATTER = new DecimalFormat("##.###");
  static { FORMATTER.setMinimumFractionDigits(3); }

  private final String _futurePrefix;
  private final String _postfix;
  private final String _dataFieldName;
  private final String _tickerScheme;

  /**
   * @param futurePrefix Two character string representing future type. e.g ED, ER, IR (See WIR in BBG)
   * @param postfix Generally, "Comdty" 
   * @param dataFieldName Expecting MarketDataRequirementNames.MARKET_PRICE
   * @param tickerScheme Expecting BLOOMBERG_TICKER_WEAK or BLOOMBERG_TICKER
   */
  public BloombergIRFuturePriceCurveInstrumentProvider(final String futurePrefix, final String postfix, final String dataFieldName, String tickerScheme) {
    Validate.notNull(futurePrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    Validate.notNull(tickerScheme, "tickerScheme was null. Try BLOOMBERG_TICKER_WEAK or BLOOMBERG_TICKER");
    _futurePrefix = futurePrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _tickerScheme = tickerScheme;
  }

  /** If a 4th argument is not provided, constructor uses BLOOMBERG_TICKER_WEAK as its ExternalScheme 
   * @param futurePrefix Two character string representing future type. e.g ED, ER, IR (See WIR in BBG)
   * @param postfix Generally, "Comdty" 
   * @param dataFieldName Expecting MarketDataRequirementNames.MARKET_PRICE 
   */
  public BloombergIRFuturePriceCurveInstrumentProvider(final String futurePrefix, final String postfix, final String dataFieldName) {
    Validate.notNull(futurePrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    _futurePrefix = futurePrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _tickerScheme = "BLOOMBERG_TICKER_WEAK";
  }

  @Override
  public ExternalId getInstrument(final Number futureNumber) {
    throw new OpenGammaRuntimeException("Need a surface date to create an interest rate future option surface");
  }

  @Override
  /**
   * Provides ExternalID for Bloomberg ticker, eg EDZ3 Comdty,
   * given a reference date and an integer offset, the n'th subsequent future
   * The format is _futurePrefix + month + year + _postfix
   * 
   * @param futureNumber n'th future following curve date
   * @param curveDate date of curve validity; valuation date
   */
  public ExternalId getInstrument(final Number futureNumber, final LocalDate curveDate) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_futurePrefix);
    ticker.append(BloombergIRFutureUtils.getQuarterlyExpiryCodeForFutures(_futurePrefix, futureNumber.intValue(), curveDate));
    ticker.append(" ");
    ticker.append(_postfix);
    return ExternalId.of(ExternalScheme.of(_tickerScheme), ticker.toString());
  }

  public String getFuturePrefix() {
    return _futurePrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getTickerScheme() {
    return _tickerScheme;
  }
  
  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public int hashCode() {
    return getFuturePrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergIRFuturePriceCurveInstrumentProvider)) {
      return false;
    }
    final BloombergIRFuturePriceCurveInstrumentProvider other = (BloombergIRFuturePriceCurveInstrumentProvider) obj;
    return getFuturePrefix().equals(other.getFuturePrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
           getDataFieldName().equals(other.getDataFieldName());
  }
}
