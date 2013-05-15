/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Autogenerates Bloomberg FX option volatility surface codes given a tenor, quote type (ATM, butterfly, risk reversal) and distance from
 * ATM.
 */
public class BloombergFXOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> {
  /** Type of the volatility quote */
  public enum FXVolQuoteType {
    /** ATM */
    ATM,
    /** Risk-reversal */
    RISK_REVERSAL,
    /** Butterfly */
    BUTTERFLY;
  }
  /** The FX prefix */
  private final String _fxPrefix; //expecting something like USDJPY
  /** The postfix */
  private final String _postfix; //expecting Curncy
  /** The data field name */
  private final String _dataFieldName; //expecting MarketDataRequirementNames.MARKET_VALUE
  /** The Bloomberg scheme name */
  private final ExternalScheme _scheme; // e.g. BLOOMBERG_TICKER_WEAK

  /**
   * Sets the scheme to weak tickers.
   * @param fxPrefix The FX prefix, not null
   * @param postfix The postfix, not null
   * @param dataFieldName The data field name, not null
   */
  public BloombergFXOptionVolatilitySurfaceInstrumentProvider(final String fxPrefix, final String postfix, final String dataFieldName) {
    this(fxPrefix, postfix, dataFieldName, ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName());
  }

  /**
   * @param fxPrefix The FX prefix, not null
   * @param postfix The code postfix, not null
   * @param dataFieldName The data field name, not null
   * @param schemeName The scheme name, not null. Must be one of {@link ExternalSchemes#BLOOMBERG_BUID}, {@link ExternalSchemes#BLOOMBERG_BUID_WEAK},
   * {@link ExternalSchemes#BLOOMBERG_TCM}, {@link ExternalSchemes#BLOOMBERG_TICKER} or {@link ExternalSchemes#BLOOMBERG_TICKER_WEAK}
   */
  public BloombergFXOptionVolatilitySurfaceInstrumentProvider(final String fxPrefix, final String postfix, final String dataFieldName,
      final String schemeName) {
    ArgumentChecker.notNull(fxPrefix, "fx prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    ArgumentChecker.notNull(schemeName, "scheme name");
    final boolean schemeTest = schemeName.equals(ExternalSchemes.BLOOMBERG_BUID.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_TCM.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
        schemeName.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName());
    ArgumentChecker.isTrue(schemeTest, "scheme name {} was not appropriate for Bloomberg data");
    _fxPrefix = fxPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _scheme = ExternalScheme.of(schemeName);
  }

  /**
   * Gets the FX prefix.
   * @return The FX prefix
   */
  public String getFXPrefix() {
    return _fxPrefix;
  }

  /**
   * Gets the code postfix.
   * @return The code postfix
   */
  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  /**
   * Gets the scheme name.
   * @return The scheme name
   */
  public String getSchemeName() {
    return _scheme.getName();
  }

  @Override
  public ExternalId getInstrument(final Tenor tenor, final Pair<Number, FXVolQuoteType> volDeltaQuoteType) {
    return createFXVolatilityCode(tenor, volDeltaQuoteType);
  }

  @Override
  public ExternalId getInstrument(final Tenor tenor, final Pair<Number, FXVolQuoteType> volDeltaQuoteType, final LocalDate surfaceDate) {
    return createFXVolatilityCode(tenor, volDeltaQuoteType);
  }

  private ExternalId createFXVolatilityCode(final Tenor tenor, final Pair<Number, FXVolQuoteType> volDeltaQuoteType) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_fxPrefix);
    final int delta = volDeltaQuoteType.getFirst().intValue();
    final FXVolQuoteType quoteType = volDeltaQuoteType.getSecond();
    String bbgCode = "";
    if (delta == 0) {
      if (quoteType == FXVolQuoteType.ATM) {
        bbgCode += "V";
      } else {
        throw new OpenGammaRuntimeException("Asked for an ATM code with non-zero delta");
      }
    } else {
      switch (quoteType) {
        case ATM:
          throw new OpenGammaRuntimeException("Asked for an ATM code with non-zero delta");
        case RISK_REVERSAL:
          bbgCode += delta + "R";
          break;
        case BUTTERFLY:
          bbgCode += delta + "B";
          break;
        default:
          throw new OpenGammaRuntimeException("Should never happen - have all quote types in enum");
      }
    }
    //TODO I'm sure this isn't the best way to do this
    if (tenor.getPeriod().getYears() != 0) {
      bbgCode += tenor.getPeriod().getYears() + "Y";
    } else if (tenor.getPeriod().getMonths() != 0) {
      bbgCode += tenor.getPeriod().getMonths() + "M";
    } else if (tenor.getPeriod().getDays() != 0 && tenor.getPeriod().getDays() % 7 == 0) {
      bbgCode += tenor.getPeriod().getDays() / 7 + "W";
    } else {
      throw new OpenGammaRuntimeException("Can only handle periods of year, month and week");
    }
    ticker.append(bbgCode);
    ticker.append(" ");
    ticker.append(_postfix);
    return ExternalId.of(_scheme, ticker.toString());
  }

  @Override
  public int hashCode() {
    return getFXPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode() + getSchemeName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BloombergFXOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergFXOptionVolatilitySurfaceInstrumentProvider other = (BloombergFXOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFXPrefix().equals(other.getFXPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getDataFieldName().equals(other.getDataFieldName()) &&
        getSchemeName().equals(other.getSchemeName());
  }
}
