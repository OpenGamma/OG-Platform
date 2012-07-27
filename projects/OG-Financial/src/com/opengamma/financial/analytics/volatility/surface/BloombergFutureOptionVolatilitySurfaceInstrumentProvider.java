/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;

/**
 * Provides ExternalId's for FutureOptions used to build the Volatility Surface
 */
public abstract class BloombergFutureOptionVolatilitySurfaceInstrumentProvider implements CallPutSurfaceInstrumentProvider<Number, Double> {

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
  protected BloombergFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike) {
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
  /**
   * Primary method of class builds up a single Bloomberg ticker
   * @return Ticker ID
   * @param futureOptionNumber n'th future after surfaceDate
   * @param strike absolute value of strike
   * @param surfaceDate valuation date
   */
  public abstract ExternalId getInstrument(final Number futureOptionNumber, final Double strike, final LocalDate surfaceDate);

  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike) {
    throw new OpenGammaRuntimeException("Need a surface date to create an future option surface");
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
    if (!(obj instanceof BloombergFutureOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergFutureOptionVolatilitySurfaceInstrumentProvider other = (BloombergFutureOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFutureOptionPrefix().equals(other.getFutureOptionPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        useCallAboveStrike().equals(other.useCallAboveStrike()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
