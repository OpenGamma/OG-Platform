/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains a curve node and the information necessary to get market data from the engine.
 */
public class CurveNodeWithIdentifier implements Comparable<CurveNodeWithIdentifier> {
  /** The node */
  private final CurveNode _node;
  /** The market data id */
  private final ExternalId _id;
  /** The data field id */
  private final String _dataField;
  /** The data field type */
  private final DataFieldType _fieldType;

  /**
   * @param node The curve node, not null
   * @param id The market data id, not null
   * @param dataField The data field, not null
   * @param fieldType The field type, not null
   */
  public CurveNodeWithIdentifier(final CurveNode node, final ExternalId id, final String dataField, final DataFieldType fieldType) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(dataField, "data field");
    ArgumentChecker.notNull(fieldType, "field type");
    _node = node;
    _id = id;
    _dataField = dataField;
    _fieldType = fieldType;
  }

  /**
   * Gets the curve node.
   * @return The curve node
   */
  public CurveNode getCurveNode() {
    return _node;
  }

  /**
   * Gets the market data identifier.
   * @return The market data identifier
   */
  public ExternalId getIdentifier() {
    return _id;
  }

  /**
   * Gets the market data field.
   * @return The market data field
   */
  public String getDataField() {
    return _dataField;
  }

  /**
   * Gets the market data field type.
   * @return The market data field type
   */
  public DataFieldType getFieldType() {
    return _fieldType;
  }

  @Override
  public int compareTo(final CurveNodeWithIdentifier o) {
    int result = _node.compareTo(o._node);
    if (result != 0) {
      return result;
    }
    result = _id.getValue().compareTo(o._id.getValue());
    if (result != 0) {
      return result;
    }
    result = _dataField.compareTo(o._dataField);
    if (result != 0) {
      return result;
    }
    return _fieldType.compareTo(o._fieldType);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _id.hashCode();
    result = prime * result + _node.hashCode();
    result = prime * result + _dataField.hashCode();
    result = prime * result + _fieldType.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CurveNodeWithIdentifier)) {
      return false;
    }
    final CurveNodeWithIdentifier other = (CurveNodeWithIdentifier) obj;
    if (!ObjectUtils.equals(_id, other._id)) {
      return false;
    }
    if (!ObjectUtils.equals(_node, other._node)) {
      return false;
    }
    if (!ObjectUtils.equals(_dataField, other._dataField)) {
      return false;
    }
    if (_fieldType != other._fieldType) {
      return false;
    }
    return true;
  }


}
