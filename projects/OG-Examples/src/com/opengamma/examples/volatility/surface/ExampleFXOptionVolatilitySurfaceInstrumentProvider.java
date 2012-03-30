/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.volatility.surface;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
//TODO Pair<Number, FXVolQuoteType> needs to be replaced with a richer data structure that has methods getATM(), getDeltas(), getRiskReversal(int delta), getButterfly(int delta)
public class ExampleFXOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> {
  private static final ExternalScheme SCHEME = SecurityUtils.OG_SYNTHETIC_TICKER;

  private final String _fxPrefix; //expecting something like USDJPY
  private final String _postfix; //expecting Curncy
  private final String _dataFieldName; //expecting MarketDataRequirementNames.MARKET_VALUE

  public ExampleFXOptionVolatilitySurfaceInstrumentProvider(final String fxPrefix, final String postfix, final String dataFieldName) {
    ArgumentChecker.notNull(fxPrefix, "fx prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _fxPrefix = fxPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
  }

  public String getFXPrefix() {
    return _fxPrefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
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
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  @Override
  public int hashCode() {
    return getFXPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExampleFXOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final ExampleFXOptionVolatilitySurfaceInstrumentProvider other = (ExampleFXOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFXPrefix().equals(other.getFXPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
