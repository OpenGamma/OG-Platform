/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides ExternalIds for future options used to build a volatility surface
 */
public abstract class BloombergFutureOptionVolatilitySurfaceInstrumentProvider implements CallPutSurfaceInstrumentProvider<Number, Double> {
  /** The future option prefix */
  private final String _futureOptionPrefix;
  /** The ticker postfix */
  private final String _postfix;
  /** The data field name */
  private final String _dataFieldName;
  /** The value above which to use calls */
  private final Double _useCallAboveStrike;
  /** The exchange name */
  private final String _exchangeIdName;
  /** THe scheme */
  private final ExternalScheme _scheme;

  /**
   * Uses the default ticker scheme (BLOOMBERG_TICKER_WEAK).
   * @param futureOptionPrefix the prefix to the resulting code, not null
   * @param postfix the postfix to the resulting code, not null
   * @param dataFieldName the name of the data field, not null.
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the id of the exchange, not null
   */
  public BloombergFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName) {
    this(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName, ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName());
  }

  /**
   * @param futureOptionPrefix the prefix to the resulting code, not null
   * @param postfix the postfix to the resulting code, not null
   * @param dataFieldName the name of the data field, not null.
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the id of the exchange, not null
   * @param schemeName the name of the scheme, not null
   */
  public BloombergFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
        final String exchangeIdName, final String schemeName) {

    ArgumentChecker.notNull(futureOptionPrefix, "future option prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    ArgumentChecker.notNull(useCallAboveStrike, "use call above this strike");
    ArgumentChecker.notNull(schemeName, "scheme name");
    final boolean schemeTest = schemeName.equals(ExternalSchemes.BLOOMBERG_BUID.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_TCM.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName());
    ArgumentChecker.isTrue(schemeTest, "scheme name {} was not appropriate for Bloomberg data");
    _futureOptionPrefix = futureOptionPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _useCallAboveStrike = useCallAboveStrike;
    _exchangeIdName = exchangeIdName;
    _scheme = ExternalScheme.of(schemeName);
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

  /**
   * Gets the expiryRule calculator providing dates from offsets
   * @return ExchangeTradedInstrumentExpiryCalculator
   */
  public abstract ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator();
  
  /**
   * Gets the exchange name.
   * @return The exchange name
   */
  public String getExchangeIdName() {
    return _exchangeIdName;
  }

  /**
   * An external id with the input as the scheme and the exchange as the value.
   * @param scheme The external scheme
   * @return An external id
   */
  public ExternalId getExchangeId(final ExternalScheme scheme) {
    return ExternalId.of(scheme, _exchangeIdName);
  }

  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike) {
    throw new OpenGammaRuntimeException("Need a surface date to create an future option surface");
  }

  /**
   * Gets the future option prefix.
   * @return The future option prefix
   */
  public String getFutureOptionPrefix() {
    return _futureOptionPrefix;
  }

  /**
   * Gets the postfix for the Bloomberg ticker
   * @return The postfix
   */
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

  /**
   * Gets the scheme.
   * @return The scheme
   */
  public ExternalScheme getScheme() {
    return _scheme;
  }

  /**
   * Gets the scheme name.
   * @return The scheme name
   */
  public String getSchemeName() {
    return _scheme.getName();
  }

  @Override
  public int hashCode() {
    return getFutureOptionPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode() + useCallAboveStrike().hashCode()
        + getExchangeIdName().hashCode() + getScheme().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BloombergFutureOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergFutureOptionVolatilitySurfaceInstrumentProvider other = (BloombergFutureOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFutureOptionPrefix().equals(other.getFutureOptionPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        useCallAboveStrike().equals(other.useCallAboveStrike()) &&
        getDataFieldName().equals(other.getDataFieldName()) &&
        getExchangeIdName().equals(other.getExchangeIdName());
  }
}
