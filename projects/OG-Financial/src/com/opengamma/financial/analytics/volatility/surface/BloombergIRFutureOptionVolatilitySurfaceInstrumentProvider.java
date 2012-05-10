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
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Provides ExternalId's for IRFutureOptions used to build the Volatility Surface  
 */
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider implements CallPutSurfaceInstrumentProvider<Number, Double> {
  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER_WEAK;
  private static final DecimalFormat FORMATTER = new DecimalFormat("##.###");
  static { FORMATTER.setMinimumFractionDigits(3); }

  private final String _futureOptionPrefix;
  private final String _postfix;
  private final String _dataFieldName; 
  private final Double _useCallAboveStrike;

  /**
   * @param futureOptionPrefix the prefix to the resulting code
   * @param postfix the postfix to the resulting code
   * @param dataFieldName the name of the data field. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts
   */
  public BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike) {
    Validate.notNull(futureOptionPrefix, "future option prefix");
    Validate.notNull(postfix, "postfix");
    Validate.notNull(dataFieldName, "data field name");
    Validate.notNull(useCallAboveStrike, "use call above this strike");
    _futureOptionPrefix = futureOptionPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _useCallAboveStrike = useCallAboveStrike;
  }

  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike) {
    throw new OpenGammaRuntimeException("Need a surface date to create an interest rate future option surface");
  }

  @Override
  /**
   * Provides ExternalID for Bloomberg ticker, eg EDZ3C  99.250 Comdty,
   * given a reference date and an integer offset, the n'th subsequent future
   * The format is _futurePrefix + month + year + _postfix
   * 
   * @param futureNumber n'th future following curve date
   * @param strike option's strike, expressed as price in %, e.g. 98.750
   * @param surfaceDate date of curve validity; valuation date
   */
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_futureOptionPrefix);
    ticker.append(BloombergIRFutureUtils.getExpiryCodeForFutureOptions(_futureOptionPrefix, futureOptionNumber.intValue(), surfaceDate));
    ticker.append(strike > _useCallAboveStrike ? "C " : "P ");
    ticker.append(FORMATTER.format(strike));
    ticker.append(" ");
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  public String getFutureOptionPrefix() {
    return _futureOptionPrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public Double useCallAboveStrike() {
    return _useCallAboveStrike;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public int hashCode() {
    return getFutureOptionPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode() + useCallAboveStrike().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider other = (BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFutureOptionPrefix().equals(other.getFutureOptionPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        useCallAboveStrike().equals(other.useCallAboveStrike()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
