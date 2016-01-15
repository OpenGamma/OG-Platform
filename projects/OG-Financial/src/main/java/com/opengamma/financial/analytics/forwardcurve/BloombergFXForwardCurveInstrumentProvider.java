/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import java.io.Serializable;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class BloombergFXForwardCurveInstrumentProvider implements ForwardCurveInstrumentProvider, Serializable {
  private static final String DATA_FIELD = MarketDataRequirementNames.MARKET_VALUE;
  private static final DataFieldType FIELD_TYPE = DataFieldType.OUTRIGHT;
  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER;
  private final String _prefix;
  private final String _postfix;
  private final String _spotPrefix;
  private final String _dataFieldName;
  private final String _spotName;
  private final ExternalId _spotId;

  public BloombergFXForwardCurveInstrumentProvider(final String prefix, final String postfix, final String spotPrefix, final String dataFieldName) {
    ArgumentChecker.notNull(prefix, "prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(spotPrefix, "spot prefix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _prefix = prefix;
    _postfix = postfix;
    _spotPrefix = spotPrefix;
    _dataFieldName = dataFieldName;
    _spotName = spotPrefix + " " + _postfix;
    _spotId = ExternalId.of(SCHEME, _spotName);
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

  public String getSpotName() {
    return _spotName;
  }

  @Override
  public ExternalId getSpotInstrument() {
    return _spotId;
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
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFutureFromTenor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final int startIMMPeriods, final int endIMMPeriods) {
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
        getSpotPrefix().equals(other.getSpotPrefix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }

}
