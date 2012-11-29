/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.SoybeanFutureExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *  Provider of commodity Future Instrument ID's.
 */
public class BloombergCommodityFuturePriceCurveInstrumentProvider implements FuturePriceCurveInstrumentProvider<Number> {

  private static final BiMap<String, ExchangeTradedInstrumentExpiryCalculator> EXPIRY_RULES;
  static {
    EXPIRY_RULES = HashBiMap.create();
    EXPIRY_RULES.put("S ", SoybeanFutureExpiryCalculator.getInstance());
  }

  private final String _futurePrefix;
  private final String _postfix;
  private final String _dataFieldName;
  private final String _tickerScheme;

  /**
   * @param futurePrefix Two character string representing future type. e.g. S  , AA etc.
   * @param postfix Generally, "Comdty"
   * @param dataFieldName Expecting MarketDataRequirementNames.MARKET_VALUE
   * @param tickerScheme Expecting BLOOMBERG_TICKER_WEAK or BLOOMBERG_TICKER
   */
  public BloombergCommodityFuturePriceCurveInstrumentProvider(final String futurePrefix, final String postfix, final String dataFieldName, String tickerScheme) {
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
   * @param futurePrefix Two character string representing future type. e.g. S  , AA etc.
   * @param postfix Generally, "Comdty"
   * @param dataFieldName Expecting MarketDataRequirementNames.MARKET_PRICE
   */
  public BloombergCommodityFuturePriceCurveInstrumentProvider(final String futurePrefix, final String postfix, final String dataFieldName) {
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
    throw new OpenGammaRuntimeException("Provider needs a curve date to create interest rate future identifier from futureNumber");
  }

  @Override
  /**
   * Provides an ExternalID for Bloomberg ticker,
   * given a reference date and an integer offset, the n'th subsequent option <p>
   * The format is prefix + postfix <p>
   * e.g. S U3 Comdty
   * <p>
   * @param futureOptionNumber n'th future following curve date, not null
   * @param strike option's strike, not null
   * @param curveDate date of future validity; valuation date, not null
   * @return the id of the Bloomberg ticker
   */
  public ExternalId getInstrument(final Number futureNumber, final LocalDate curveDate) {
    ArgumentChecker.notNull(futureNumber, "futureOptionNumber");
    ArgumentChecker.notNull(curveDate, "curve date");
    final StringBuffer ticker = new StringBuffer();
    ticker.append(getFuturePrefix());
    final ExchangeTradedInstrumentExpiryCalculator expiryRule = EXPIRY_RULES.get(getFuturePrefix());
    if (expiryRule == null) {
      throw new OpenGammaRuntimeException("No expiry rule has been setup for " + getFuturePrefix() + ". Determine week and day pattern and add to EXPIRY_RULES.");
    }
    final LocalDate expiryDate = expiryRule.getExpiryMonth(futureNumber.intValue(), curveDate);
    final String expiryCode = BloombergFutureUtils.getShortExpiryCode(expiryDate);
    ticker.append(expiryCode);
    ticker.append(" ");
    ticker.append(getPostfix());
    return ExternalId.of(getTickerScheme(), ticker.toString());
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
    if (!(obj instanceof BloombergCommodityFuturePriceCurveInstrumentProvider)) {
      return false;
    }
    final BloombergCommodityFuturePriceCurveInstrumentProvider other = (BloombergCommodityFuturePriceCurveInstrumentProvider) obj;
    return getFuturePrefix().equals(other.getFuturePrefix()) &&
           getPostfix().equals(other.getPostfix()) &&
           getDataFieldName().equals(other.getDataFieldName());
  }
}
