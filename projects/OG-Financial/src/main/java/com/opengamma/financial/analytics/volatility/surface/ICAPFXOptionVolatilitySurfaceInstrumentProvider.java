/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Autogenerates ICAP FX option volatility surface codes given a tenor, quote type (ATM, butterfly, risk reversal) and distance from
 * ATM.
 */
public class ICAPFXOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> {
  /** Butterfly */
  private static final String BF_STRING = "BF";
  /** Risk reversal */
  private static final String RR_STRING = "RR";
  /** String representing years */
  private static final String YR_STRING = "YR";
  /** String representing months */
  private static final String M_STRING = "M";
  /** String representing weeks */
  private static final String WK_STRING = "WK";
  /** The ICAP scheme */
  private static final ExternalScheme SCHEME = ExternalSchemes.ICAP;
  /** The prefix */
  private final String _fxPrefix;
  /** The currency pair */
  private final String _ccyPair;
  /** The data field name */
  private final String _dataFieldName;

  /**
   * @param fxPrefix The code prefix, not null
   * @param ccyPair The currency pair, not null
   * @param dataFieldName The data field name, not null
   */
  public ICAPFXOptionVolatilitySurfaceInstrumentProvider(final String fxPrefix, final String ccyPair, final String dataFieldName) {
    ArgumentChecker.notNull(fxPrefix, "fx prefix");
    ArgumentChecker.notNull(ccyPair, "curreny pair");
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
  public ExternalId getInstrument(final Tenor xAxis, final Pair<Number, FXVolQuoteType> yAxis) {
    ArgumentChecker.notNull(xAxis, "x axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    final StringBuffer ticker = new StringBuffer(_fxPrefix);
    ticker.append(_ccyPair);
    final int delta = yAxis.getFirst().intValue();
    final FXVolQuoteType quoteType = yAxis.getSecond();
    if (delta != 0) {
      switch (quoteType) {
        case ATM:
          throw new OpenGammaRuntimeException("Asked for an ATM code with non-zero delta");
        case RISK_REVERSAL:
          ticker.append(RR_STRING);
          ticker.append(delta);
          break;
        case BUTTERFLY:
          ticker.append(BF_STRING);
          ticker.append(delta);
          break;
        default:
          throw new OpenGammaRuntimeException("Did not recognise quote type " + quoteType);
      }
    }
    ticker.append("_");
    final Period period = xAxis.getPeriod();
    if (period.getYears() != 0) {
      ticker.append(period.getYears());
      ticker.append(YR_STRING);
    } else if (period.getMonths() != 0) {
      ticker.append(period.getMonths());
      ticker.append(M_STRING);
    } else if (period.getDays() != 0) {
      if (period.getDays() % 7 != 0) {
        throw new OpenGammaRuntimeException("Can only handle tenors with periods in weeks; have " + period.getDays());
      }
      ticker.append(period.getDays() / 7);
      ticker.append(WK_STRING);
    }
    return ExternalId.of(SCHEME, ticker.toString());
  }

  @Override
  public ExternalId getInstrument(final Tenor xAxis, final Pair<Number, FXVolQuoteType> yAxis, final LocalDate surfaceDate) {
    return getInstrument(xAxis, yAxis);
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ccyPair.hashCode();
    result = prime * result + _dataFieldName.hashCode();
    result = prime * result + _fxPrefix.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ICAPFXOptionVolatilitySurfaceInstrumentProvider other = (ICAPFXOptionVolatilitySurfaceInstrumentProvider) obj;
    if (!ObjectUtils.equals(_ccyPair, other._ccyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_fxPrefix, other._fxPrefix)) {
      return false;
    }
    if (!ObjectUtils.equals(_dataFieldName, other._dataFieldName)) {
      return false;
    }
    return true;
  }

}
