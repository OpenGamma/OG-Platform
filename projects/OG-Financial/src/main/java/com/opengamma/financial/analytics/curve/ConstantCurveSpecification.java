/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ExternalId;

/**
 * Specification for a constant curve.
 */
public class ConstantCurveSpecification extends AbstractCurveSpecification {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The identifier of the data */
  private final ExternalId _identifier;
  /** The data field */
  private final String _dataField;

  /**
   * @param curveDate The curve date, not null
   * @param name The curve name, not null
   * @param identifier The identifier of the data, not null
   * @param dataField The data field. If this value is null, it will be set to {@link MarketDataRequirementNames#MARKET_VALUE}.
   */
  public ConstantCurveSpecification(final LocalDate curveDate, final String name, final ExternalId identifier, final String dataField) {
    super(curveDate, name);
    _identifier = identifier;
    _dataField = dataField == null ? MarketDataRequirementNames.MARKET_VALUE : dataField;
  }

  /**
   * Gets the identifier.
   * @return the identifier
   */
  public ExternalId getIdentifier() {
    return _identifier;
  }

  /**
   * Gets the data field.
   * @return the data field
   */
  public String getDataField() {
    return _dataField;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _dataField.hashCode();
    result = prime * result + _identifier.hashCode();
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
    if (!(obj instanceof ConstantCurveSpecification)) {
      return false;
    }
    final ConstantCurveSpecification other = (ConstantCurveSpecification) obj;
    if (!ObjectUtils.equals(_identifier, other._identifier)) {
      return false;
    }
    if (!ObjectUtils.equals(_dataField, other._dataField)) {
      return false;
    }
    return true;
  }

}
