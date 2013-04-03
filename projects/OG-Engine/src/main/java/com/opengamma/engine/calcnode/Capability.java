/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Capability exported by a {@link JobInvoker} about the node(s) it is invoking jobs on, or a requirement
 * of a job.
 */
public final class Capability implements Comparable<Capability> {

  private final String _identifier;
  private final Double _parameterLow;
  private final Double _parameterHigh;

  private Capability(final String identifier, final Double parameterLow, final Double parameterHigh) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
    _parameterLow = parameterLow;
    _parameterHigh = parameterHigh;
  }

  public static Capability instanceOf(final String identifier) {
    return new Capability(identifier, null, null);
  }

  public static Capability parameterInstanceOf(final String identifier, final double parameter) {
    return new Capability(identifier, parameter, parameter);
  }

  public static Capability lowerBoundInstanceOf(final String identifier, final double lowerBoundParameter) {
    return new Capability(identifier, lowerBoundParameter, null);
  }

  public static Capability upperBoundInstanceOf(final String identifier, final double upperBoundParameter) {
    return new Capability(identifier, null, upperBoundParameter);
  }

  public static Capability boundedInstanceOf(final String identifier, final double lowerBoundParameter, final double upperBoundParameter) {
    ArgumentChecker.isTrue(lowerBoundParameter <= upperBoundParameter, "lower bound must be less than upper bound");
    return new Capability(identifier, lowerBoundParameter, upperBoundParameter);
  }

  public String getIdentifier() {
    return _identifier;
  }

  public Double getLowerBoundParameter() {
    return _parameterLow;
  }

  public Double getUpperBoundParameter() {
    return _parameterHigh;
  }

  @Override
  public int hashCode() {
    final int multiplier = 17;
    int hc = 1;
    hc += getIdentifier().hashCode();
    hc *= multiplier;
    if (getLowerBoundParameter() != null) {
      hc += getLowerBoundParameter().hashCode();
    }
    hc *= multiplier;
    if (getUpperBoundParameter() != null) {
      hc += getUpperBoundParameter().hashCode();
    }
    return hc;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Capability)) {
      return false;
    }
    final Capability other = (Capability) o;
    return ObjectUtils.equals(getIdentifier(), other.getIdentifier()) && ObjectUtils.equals(getLowerBoundParameter(), other.getLowerBoundParameter())
        && ObjectUtils.equals(getUpperBoundParameter(), other.getUpperBoundParameter());
  }

  /**
   * Capabilities are ordered by identifier, then by lower bound (with unbounded before bounded), then by upper bound (with bounded before unbounded).
   * 
   * @param o capability to compare to
   * @return result of the comparison
   */
  @Override
  public int compareTo(Capability o) {
    int cmp = getIdentifier().compareTo(o.getIdentifier());
    if (cmp != 0) {
      return cmp;
    }
    if (getLowerBoundParameter() == null) {
      if (o.getLowerBoundParameter() != null) {
        return -1;
      }
    } else {
      if (o.getLowerBoundParameter() == null) {
        return 1;
      } else {
        cmp = getLowerBoundParameter().compareTo(o.getLowerBoundParameter());
        if (cmp != 0) {
          return cmp;
        }
      }
    }
    if (getUpperBoundParameter() != null) {
      if (o.getUpperBoundParameter() != null) {
        return getUpperBoundParameter().compareTo(o.getUpperBoundParameter());
      } else {
        return -1;
      }
    } else {
      if (o.getUpperBoundParameter() != null) {
        return 1;
      } else {
        return 0;
      }
    }
  }

}
