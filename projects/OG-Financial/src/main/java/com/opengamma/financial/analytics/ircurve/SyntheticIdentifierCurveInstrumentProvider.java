/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.LocalDate;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * This should be pulled from the configuration.
 */
public class SyntheticIdentifierCurveInstrumentProvider implements CurveInstrumentProvider {
  private final Currency _ccy;
  private final StripInstrumentType _type;
  private StripInstrumentType _idType;
  private final ExternalScheme _scheme;
  private final String _dataField;
  private final DataFieldType _fieldType;

  public SyntheticIdentifierCurveInstrumentProvider(final Currency ccy, final StripInstrumentType type, final ExternalScheme scheme) {
    this(ccy, type, scheme, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT);
  }

  public SyntheticIdentifierCurveInstrumentProvider(final Currency ccy, final StripInstrumentType type, final ExternalScheme scheme,
      final String dataField, final DataFieldType fieldType) {
    ArgumentChecker.notNull(ccy, "currency");
    ArgumentChecker.notNull(type, "instrument type");
    ArgumentChecker.notNull(scheme, "generated identifier scheme");
    ArgumentChecker.notNull(dataField, "data field");
    ArgumentChecker.notNull(fieldType, "field type");
    _ccy = ccy;
    _type = type;
    _scheme = scheme;
    _dataField = dataField;
    _fieldType = fieldType;
    switch (type) {
      case SWAP_3M:
      case SWAP_6M:
      case SWAP_12M:
        _idType = StripInstrumentType.SWAP;
        break;
      case FRA_3M:
      case FRA_6M:
        _idType = StripInstrumentType.FRA;
        break;
      default:
        _idType = type;
        break;
    }
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + tenor.getPeriod().toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + tenor.getPeriod().toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numQuarterlyFuturesFromTenor) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + startTenor.getPeriod().toString() + futureTenor.getPeriod().toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + Integer.toString(periodsPerYear) + tenor.getPeriod().toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + "_" + payIndexType.name() + payTenor.getPeriod().toString() + receiveIndexType.name()
        + receiveTenor.getPeriod().toString() + "_" + tenor.getPeriod().toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + "_" + indexType + resetTenor.getPeriod().toString() + "_" + tenor.getPeriod().toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final int startIMMPeriods, final int endIMMPeriods) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + startTenor.getPeriod().toString() + "_" + startIMMPeriods + "_" + endIMMPeriods);
  }

  public Currency getCurrency() {
    return _ccy;
  }

  public StripInstrumentType getType() {
    return _type;
  }

  public ExternalScheme getScheme() {
    return _scheme;
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
    if (!(o instanceof SyntheticIdentifierCurveInstrumentProvider)) {
      return false;
    }
    final SyntheticIdentifierCurveInstrumentProvider other = (SyntheticIdentifierCurveInstrumentProvider) o;
    return _ccy.equals(other._ccy) &&
        _type.equals(other._type) &&
        _scheme.equals(other._scheme) &&
        _dataField.equals(other._dataField) &&
        _fieldType.equals(other._fieldType);
  }

  @Override
  public int hashCode() {
    return _ccy.hashCode() ^ (_type.hashCode() * 64);
  }

}
