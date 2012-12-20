/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
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
public class ICAPFXOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> {
  private static final String BF_STRING = "BF";
  private static final String RR_STRING = "RR";
  private static final String YR_STRING = "YR";
  private static final String M_STRING = "M";
  private static final String WK_STRING = "WK";
  private static final ExternalScheme SCHEME = ExternalSchemes.ICAP;
  private final String _fxPrefix;
  private final String _ccyPair;
  private final String _dataFieldName;

  public ICAPFXOptionVolatilitySurfaceInstrumentProvider(final String fxPrefix, final String ccyPair, final String dataFieldName) {
    ArgumentChecker.notNull(fxPrefix, "fx prefix");
    ArgumentChecker.notNull(ccyPair, "curreny pair");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _fxPrefix = fxPrefix;
    _ccyPair = ccyPair;
    _dataFieldName = dataFieldName;
  }

  public String getFXPrefix() {
    return _fxPrefix;
  }

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
