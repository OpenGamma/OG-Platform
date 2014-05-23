/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

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
 * Specification for a curve that contains a sorted set of {@link CurveNodeWithIdentifier}.
 */
public class CurveSpecification extends AbstractCurveSpecification {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The curve nodes */
  private final SortedSet<CurveNodeWithIdentifier> _nodes;

  /**
   * @param curveDate The curve date, not null
   * @param name The curve name, not null
   * @param nodes The nodes that are used to construct this curve, not null
   */
  public CurveSpecification(final LocalDate curveDate, final String name, final Collection<CurveNodeWithIdentifier> nodes) {
    super(curveDate, name);
    ArgumentChecker.notNull(nodes, "nodes");
    _nodes = new TreeSet<>(nodes);
  }

  /**
   * Adds a node to this specification.
   * @param node The node, not null
   */
  public void addNode(final CurveNodeWithIdentifier node) {
    ArgumentChecker.notNull(node, "nodes");
    _nodes.add(node);
  }

  /**
   * Gets the curve nodes.
   * @return The curve nodes.
   */
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
    int result = super.hashCode();
    result = prime * result + ((_nodes == null) ? 0 : _nodes.hashCode());
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
    if (!(obj instanceof CurveSpecification)) {
      return false;
    }
    final CurveSpecification other = (CurveSpecification) obj;
    if (!ObjectUtils.equals(_nodes, other._nodes)) {
      return false;
    }
    return true;
  }

}
