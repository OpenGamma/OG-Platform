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
 * 
 */
public class CurveNodeWithIdentifier implements Comparable<CurveNodeWithIdentifier> {
  private final CurveNode _node;
  private final ExternalId _id;
  private final String _dataField;
  private final DataFieldType _fieldType;

  public CurveNodeWithIdentifier(final CurveNode node, final ExternalId id, final String dataField, final DataFieldType fieldType) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(dataField, "data field");
    _node = node;
    _id = id;
    _dataField = dataField;
    _fieldType = fieldType;
  }

  public CurveNode getCurveNode() {
    return _node;
  }

  public ExternalId getIdentifier() {
    return _id;
  }

  public String getDataField() {
    return _dataField;
  }

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
    if (obj == null) {
      return false;
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
