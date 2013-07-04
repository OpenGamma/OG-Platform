/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CurveSpecification implements Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The curve date */
  private final LocalDate _curveDate;
  /** The curve name */
  private final String _name;
  /** The curve nodes */
  private final SortedSet<CurveNodeWithIdentifier> _nodes;

  public CurveSpecification(final LocalDate curveDate, final String name, final Collection<CurveNodeWithIdentifier> nodes) {
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(nodes, "nodes");
    _curveDate = curveDate;
    _name = name;
    _nodes = new TreeSet<>(nodes);
  }

  public void addNode(final CurveNodeWithIdentifier node) {
    ArgumentChecker.notNull(node, "nodes");
    _nodes.add(node);
  }

  public LocalDate getCurveDate() {
    return _curveDate;
  }

  public String getName() {
    return _name;
  }

  public Set<CurveNodeWithIdentifier> getNodes() {
    return Collections.unmodifiableSet(_nodes);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveDate.hashCode();
    result = prime * result + _name.hashCode();
    result = prime * result + _nodes.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CurveSpecification)) {
      return false;
    }
    final CurveSpecification other = (CurveSpecification) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveDate, other._curveDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_nodes, other._nodes)) {
      return false;
    }
    return true;
  }

}
