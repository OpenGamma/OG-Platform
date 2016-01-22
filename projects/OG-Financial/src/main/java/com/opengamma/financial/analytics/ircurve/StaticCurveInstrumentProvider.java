/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Provides the external id of instruments for which the ticker does not change with time.
 *
 * This should be pulled from the configuration.
 */
public class StaticCurveInstrumentProvider implements CurveInstrumentProvider, Serializable {
  /** The market data identifier */
  private final ExternalId _identifier;
  /** The market data field */
  private final String _dataField;
  /** The market data field type */
  private final DataFieldType _fieldType;

  /**
   * Sets the data field for market data to {@link MarketDataRequirementNames#MARKET_VALUE}
   * @param identifier The market data identifier, not null
   */
  public StaticCurveInstrumentProvider(final ExternalId identifier) {
    this(identifier, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT);
  }

  /**
   * @param identifier The market data identifier, not null
   * @param dataField The market data field, not null
   * @param fieldType The market data field type, not null
   */
  public StaticCurveInstrumentProvider(final ExternalId identifier, final String dataField, final DataFieldType fieldType) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(dataField, "data field");
    ArgumentChecker.notNull(fieldType, "field type");
    _identifier = identifier;
    _dataField = dataField;
    _fieldType = fieldType;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFutureFromTenor) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    if (isPeriodicZeroDeposit) {
      return _identifier;
    }
    throw new OpenGammaRuntimeException("Flag indicating periodic zero deposit was false");
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    return _identifier;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final int startIMMPeriods, final int endIMMPeriods) {
    return _identifier;
  }

  @Override
  public String getMarketDataField() {
    return _dataField;
  }

  @Override
  public DataFieldType getDataFieldType() {
    return _fieldType;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof StaticCurveInstrumentProvider)) {
      return false;
    }
    final StaticCurveInstrumentProvider other = (StaticCurveInstrumentProvider) o;
    return ObjectUtils.equals(_identifier, other._identifier) &&
        ObjectUtils.equals(_dataField, other._dataField) &&
        _fieldType == other._fieldType;
  }

  @Override
  public int hashCode() {
    return _identifier.hashCode() ^ _dataField.hashCode() ^ _fieldType.hashCode();
  }

  @Override
  public String toString() {
    return "StaticCurveInstrumentProvider[" + _identifier.toString() + ", field=" + _dataField + ", type=" + _fieldType + "]";
  }

}
