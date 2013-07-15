/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.ExternalId;

/**
 * Contains a curve node for when the market data is a spread over other market data.
 */
public class PointsCurveNodeWithIdentifier extends CurveNodeWithIdentifier {
  /** The underlying market data id */
  private final ExternalId _underlyingId;
  /** The underlying market data field */
  private final String _underlyingField;

  /**
   * @param node The curve node, not null
   * @param id The market data id, not null
   * @param dataField The data field, not null
   * @param fieldType The field type, not null
   * @param underlyingId The underlying market data id, not null
   * @param underlyingField The underlying market data field, not null
   */
  public PointsCurveNodeWithIdentifier(final CurveNode node, final ExternalId id, final String dataField, final DataFieldType fieldType,
      final ExternalId underlyingId, final String underlyingField) {
    super(node, id, dataField, fieldType);
    _underlyingId = underlyingId;
    _underlyingField = underlyingField;
  }

  /**
   * Gets the underlying market data id.
   * @return The underlying market data id
   */
  public ExternalId getUnderlyingIdentifier() {
    return _underlyingId;
  }

  /**
   * Gets the underlying market data field.
   * @return The underlying market data field
   */
  public String getUnderlyingDataField() {
    return _underlyingField;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _underlyingField.hashCode();
    result = prime * result + _underlyingId.hashCode();
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
    if (!(obj instanceof PointsCurveNodeWithIdentifier)) {
      return false;
    }
    final PointsCurveNodeWithIdentifier other = (PointsCurveNodeWithIdentifier) obj;
    return ObjectUtils.equals(_underlyingId, other._underlyingId) &&
        ObjectUtils.equals(_underlyingField, other._underlyingField);
  }

}
