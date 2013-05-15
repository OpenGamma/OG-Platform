/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class BloombergAlternativeFXOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> {
  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER_WEAK;
  private final String _fxPrefix;
  private final String _postfix; //expecting Curncy
  private final String _dataFieldName; //expecting MarketDataRequirementNames.MARKET_VALUE

  public BloombergAlternativeFXOptionVolatilitySurfaceInstrumentProvider(final String fxPrefix, final String postfix, final String dataFieldName) {
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
    final StringBuffer bbgCode = new StringBuffer();
    if (quoteType == FXVolQuoteType.ATM) {
      bbgCode.append('V');
    } else if (quoteType == FXVolQuoteType.RISK_REVERSAL) {
      if (delta == 10) {
        bbgCode.append("RX");
      } else if (delta == 25) {
        bbgCode.append("RR");
      } else {
        throw new UnsupportedOperationException("Can only handle 10 and 25 delta quotes");
      }
    } else if (quoteType == FXVolQuoteType.BUTTERFLY) {
      if (delta == 10) {
        bbgCode.append("BX");
      } else if (delta == 25) {
        bbgCode.append("BX");
      } else {
        throw new UnsupportedOperationException("Can only handle 10 and 25 delta quotes");
      }
    } else {
      throw new UnsupportedOperationException("Can only handle ATM, risk reversal and butterfly quotes");
    }
    final Period period = tenor.getPeriod();
    if (period.getMonths() != 0) {
      bbgCode.append(period.getMonths());
      bbgCode.append('M');
    } else if (period.getYears() != 0) {
      bbgCode.append(period.getYears());
      if (period.getYears() < 10) {
        bbgCode.append(period.getYears());
        bbgCode.append('Y');
      }
    } else if (period.getDays() != 0 && period.getDays() % 7 == 0) {
      bbgCode.append(period.getDays() / 7);
      bbgCode.append('W');
    } else {
      throw new UnsupportedOperationException("Can only handle periods in weeks, months or years");
    }
    ticker.append(bbgCode);
    ticker.append(" ");
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
    if (!(obj instanceof BloombergAlternativeFXOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final BloombergAlternativeFXOptionVolatilitySurfaceInstrumentProvider other = (BloombergAlternativeFXOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFXPrefix().equals(other.getFXPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
