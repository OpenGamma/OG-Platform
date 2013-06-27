/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Curve instrument provider for tickers that represent a spread over a base value.
 */
public class StaticCurvePointsInstrumentProvider extends StaticCurveInstrumentProvider {
  /** The underlying market data identifier */
  private final ExternalId _underlyingIdentifier;
  /** The underlying market data field */
  private final String _underlyingDataField;

  /**
   * @param identifier The market data identifier, not null
   * @param dataField The market data field, not null
   * @param fieldType The field type, not null
   * @param underlyingIdentifier The underlying market data identifier, not null
   * @param underlyingDataField The underlying data field, not null
   */
  public StaticCurvePointsInstrumentProvider(final ExternalId identifier, final String dataField, final DataFieldType fieldType,
      final ExternalId underlyingIdentifier, final String underlyingDataField) {
    super(identifier, dataField, fieldType);
    ArgumentChecker.notNull(underlyingIdentifier, "underlying identifier");
    ArgumentChecker.notNull(underlyingDataField, "underlying data field");
    _underlyingIdentifier = underlyingIdentifier;
    _underlyingDataField = underlyingDataField;
  }

  public ExternalId getUnderlyingInstrument() {
    return _underlyingIdentifier;
  }

  public String getUnderlyingMarketDataField() {
    return _underlyingDataField;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _underlyingDataField.hashCode();
    result = prime * result + _underlyingIdentifier.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof StaticCurvePointsInstrumentProvider)) {
      return false;
    }
    final StaticCurvePointsInstrumentProvider other = (StaticCurvePointsInstrumentProvider) obj;
    return ObjectUtils.equals(_underlyingIdentifier, other._underlyingIdentifier) &&
        ObjectUtils.equals(_underlyingDataField, other._underlyingDataField);
  }

}
