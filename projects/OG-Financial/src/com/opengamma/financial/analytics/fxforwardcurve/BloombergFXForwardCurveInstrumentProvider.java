/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class BloombergFXForwardCurveInstrumentProvider implements FXForwardCurveInstrumentProvider {
  private static final ExternalScheme SCHEME = SecurityUtils.BLOOMBERG_TICKER_WEAK;
  private final String _prefix;
  private final String _postfix;
  private final String _dataFieldName;
  private final String _spotName;
  private final ExternalId _spotId;

  public BloombergFXForwardCurveInstrumentProvider(final String prefix, final String postfix, final String dataFieldName) {
    ArgumentChecker.notNull(prefix, "prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _prefix = prefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
    _spotName = prefix + " BGN " + _postfix;
    _spotId = ExternalId.of(SCHEME, _spotName);
  }

  public String getPrefix() {
    return _prefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  public String getSpotName() {
    return _spotName;
  }

  @Override
  public ExternalId getSpotInstrument() {
    return _spotId;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_prefix);
    if (tenor.getPeriod().getYears() != 0) {
      ticker.append(tenor.getPeriod().getYears() + "Y");
    } else if (tenor.getPeriod().getMonths() != 0) {
      ticker.append(tenor.getPeriod().getMonths() + "M");
    } else if (tenor.getPeriod().getDays() != 0 && tenor.getPeriod().getDays() % 7 == 0) {
      ticker.append(tenor.getPeriod().getDays() / 7 + "W");
    } else {
      throw new OpenGammaRuntimeException("Can only handle periods of year, month and week");
    }
    ticker.append(" ");
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    return getPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergFXForwardCurveInstrumentProvider)) {
      return false;
    }
    final BloombergFXForwardCurveInstrumentProvider other = (BloombergFXForwardCurveInstrumentProvider) obj;
    return getPrefix().equals(other.getPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
