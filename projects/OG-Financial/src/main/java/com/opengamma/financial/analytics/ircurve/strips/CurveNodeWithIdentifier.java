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

  public CurveNodeWithIdentifier(final CurveNode node, final ExternalId id) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(id, "id");
    _node = node;
    _id = id;
  }

  public CurveNode getCurveNode() {
    return _node;
  }

  public ExternalId getIdentifier() {
    return _id;
  }

  @Override
  public int compareTo(final CurveNodeWithIdentifier o) {
    final int result = _node.compareTo(o._node);
    if (result != 0) {
      return result;
    }
    return _id.getValue().compareTo(o._id.getValue());
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
    return true;
  }


}
