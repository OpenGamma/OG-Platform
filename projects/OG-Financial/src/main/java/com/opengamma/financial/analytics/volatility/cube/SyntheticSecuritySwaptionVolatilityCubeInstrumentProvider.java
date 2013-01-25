/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider implements CubeInstrumentProvider<Tenor, Tenor, Double> {
  private final String _prefix;
  private final String _dataFieldName;

  public SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider(final String prefix, final String dataFieldName) {
    ArgumentChecker.notNull(prefix, "external prefix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _prefix = prefix;
    _dataFieldName = dataFieldName;
  }

  @Override
  public ExternalId getInstrument(final Tenor swapMaturity, final Tenor swaptionExpiry, final Double relativeStrike) {
    ArgumentChecker.notNull(swapMaturity, "swap maturity");
    ArgumentChecker.notNull(swaptionExpiry, "swaption expiry");
    ArgumentChecker.notNull(relativeStrike, "relative strike");
    final StringBuffer ticker = new StringBuffer(_prefix);
    final String swaptionString = getTenorString(swaptionExpiry);
    final String swapString = getTenorString(swapMaturity);
    ticker.append(swaptionString);
    ticker.append(swapString);
    ticker.append(Double.toString(relativeStrike));
    return ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, ticker.toString());
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  public String getPrefix() {
    return _prefix;
  }

  private String getTenorString(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears() + "Y";
    } else if (period.getMonths() != 0) {
      return period.getMonths() + "M";
    }
    throw new OpenGammaRuntimeException("Can only handle tenors in units of months or years");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dataFieldName.hashCode();
    result = prime * result + _prefix.hashCode();
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
    final SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider other = (SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider) obj;
    return ObjectUtils.equals(_dataFieldName, other._dataFieldName) && ObjectUtils.equals(_prefix, other._prefix);
  }
}
