/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import java.io.Serializable;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class BloombergForwardSwapCurveInstrumentProvider extends ForwardSwapCurveInstrumentProvider implements Serializable {
  private static final String DATA_FIELD = MarketDataRequirementNames.MARKET_VALUE;
  private static final DataFieldType FIELD_TYPE = DataFieldType.OUTRIGHT;
  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER_WEAK;
  private static final String[] MONTHS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "1A", "1B", "1C", "1D", "1E", "1F", "1G", "1H", "1I", "1J", "1K", "1L" };
  private final String _prefix;
  private final String _postfix;
  private final String _spotPrefix;
  private final String _dataFieldName;

  public BloombergForwardSwapCurveInstrumentProvider(final String prefix, final String postfix, final String spotPrefix, final String dataFieldName) {
    ArgumentChecker.notNull(prefix, "prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(spotPrefix, "spot prefix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _prefix = prefix;
    _postfix = postfix;
    _spotPrefix = spotPrefix;
    _dataFieldName = dataFieldName;
  }

  public String getPrefix() {
    return _prefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  public String getSpotPrefix() {
    return _spotPrefix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public String getMarketDataField() {
    return DATA_FIELD;
  }

  @Override
  public DataFieldType getDataFieldType() {
    return FIELD_TYPE;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor forwardTenor) {
    final String swapCode = getTenorCode(tenor.getPeriod(), false);
    final String forwardCode = getTenorCode(forwardTenor.getPeriod(), true);
    return ExternalId.of(SCHEME, _prefix + forwardCode + swapCode + " " + _postfix);
  }

  @Override
  public ExternalId getSpotInstrument(final Tenor forwardTenor) {
    final Period period = forwardTenor.getPeriod();
    return ExternalId.of(SCHEME, getSwapCode(period));
  }

  private String getTenorCode(final Period period, final boolean prepadWithZero) {
    if (period.getYears() != 0) {
      if (period.getYears() < 10 && prepadWithZero) {
        return "0" + Integer.toString(period.getYears());
      }
      return Integer.toString(period.getYears());
    }
    if (period.getMonths() != 0) {
      final int months = period.getMonths();
      if (months > 0) {
        final String result = MONTHS[months - 1];
        if (result.length() == 1 && prepadWithZero) {
          return "0" + result;
        }
        return result;
      }
    }
    throw new OpenGammaRuntimeException("Can only handle tenors of months or years; have " + period);
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFutureFromTenor) {
    throw new UnsupportedOperationException();
  }

  private String getSwapCode(final Period period) {
    if (period.getYears() != 0) {
      final String years = Integer.toString(period.getYears());
      return _spotPrefix + years + _postfix;
    }
    if (period.getMonths() != 0) {
      return _spotPrefix + MONTHS[period.getMonths() - 1] + _postfix;
    }
    throw new OpenGammaRuntimeException("Can only handle swap tenors of months or years; have " + period);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dataFieldName.hashCode();
    result = prime * result + _postfix.hashCode();
    result = prime * result + _prefix.hashCode();
    result = prime * result + _spotPrefix.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergForwardSwapCurveInstrumentProvider)) {
      return false;
    }
    final BloombergForwardSwapCurveInstrumentProvider other = (BloombergForwardSwapCurveInstrumentProvider) obj;
    return getPrefix().equals(other.getPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getSpotPrefix().equals(other.getSpotPrefix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }

}
