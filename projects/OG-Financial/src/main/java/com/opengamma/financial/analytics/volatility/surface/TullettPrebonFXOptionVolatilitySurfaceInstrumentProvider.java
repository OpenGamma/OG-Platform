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
 * Autogenerates Tullett-Prebon FX option volatility surface codes given a tenor, quote type (ATM, butterfly, risk reversal) and distance from
 * ATM.
 */
public class TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> {
  /** The Tullett-Prebon scheme */
  private static final ExternalScheme SCHEME = ExternalSchemes.SURF;
  /** The prefix */
  private final String _fxPrefix; //expecting something like FV
  /** The currency pair */
  private final String _ccyPair; // expecting something like USDJPY
  /** The data field name */
  private final String _dataFieldName;

  /**
   * @param fxPrefix The code prefix, not null
   * @param ccyPair The currency pair, not null
   * @param dataFieldName The data field name, not null
   */
  public TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider(final String fxPrefix, final String ccyPair, final String dataFieldName) {
    ArgumentChecker.notNull(fxPrefix, "fx prefix");
    ArgumentChecker.notNull(ccyPair, "currency pair");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _fxPrefix = fxPrefix;
    _ccyPair = ccyPair;
    _dataFieldName = dataFieldName;
  }

  /**
   * Gets the code prefix.
   * @return The code prefix
   */
  public String getFXPrefix() {
    return _fxPrefix;
  }

  /**
   * Gets the currency pair.
   * @return The currency pair
   */
  public String getCurrencyPair() {
    return _ccyPair;
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
    final StringBuffer surf = new StringBuffer();
    surf.append(getFXPrefix());
    final int delta = volDeltaQuoteType.getFirst().intValue();
    final FXVolQuoteType quoteType = volDeltaQuoteType.getSecond();
    if (delta == 0) {
      if (quoteType == FXVolQuoteType.ATM) {
        surf.append("AFV");
      } else {
        throw new OpenGammaRuntimeException("Asked for an ATM code with non-zero delta");
      }
    } else {
      switch (quoteType) {
        case ATM:
          throw new OpenGammaRuntimeException("Asked for an ATM code with non-zero delta");
        case RISK_REVERSAL:
          surf.append(delta / 10); // 10 = 1, 25 = 2
          surf.append("DR");
          break;
        case BUTTERFLY:
          surf.append(delta / 10); // 10 = 1, 25 = 2;
          surf.append("DB");
          break;
        default:
          throw new OpenGammaRuntimeException("Should never happen - have all quote types in enum");
      }
    }
    surf.append(getCurrencyPair());
    //TODO I'm sure this isn't the best way to do this
    if (tenor.getPeriod().getYears() != 0) {
      if (tenor.getPeriod().getYears() < 3) {
        surf.append(String.format("%02d", tenor.getPeriod().getYears() * 12));
        surf.append("M");
      } else {
        surf.append(String.format("%02d", tenor.getPeriod().getYears()));
        surf.append("Y");
      }
    } else if (tenor.getPeriod().getMonths() != 0) {
      surf.append(String.format("%02d", tenor.getPeriod().getMonths()));
      surf.append("M");
    } else if (tenor.getPeriod().getDays() != 0 && tenor.getPeriod().getDays() % 7 == 0) {
      surf.append(String.format("%02d", tenor.getPeriod().getDays() / 7));
      surf.append("W");
    } else {
      throw new OpenGammaRuntimeException("Can only handle periods of year, month and week");
    }
    return ExternalId.of(SCHEME, surf.toString());
  }

  @Override
  public int hashCode() {
    return getFXPrefix().hashCode() + getCurrencyPair().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider other = (TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider) obj;
    return getFXPrefix().equals(other.getFXPrefix()) &&
        getCurrencyPair().equals(other.getCurrencyPair()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
