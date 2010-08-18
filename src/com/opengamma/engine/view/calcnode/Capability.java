/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Capability exported by a {@link JobInvoker} about the node(s) it is invoking jobs on, or a requirement
 * of a job.
 */
public final class Capability {

  private final String _identifier;
  private final Long _parameterLow;
  private final Long _parameterHigh;

  private Capability(final String identifier, final Long parameterLow, final Long parameterHigh) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
    _parameterLow = parameterLow;
    _parameterHigh = parameterHigh;
  }

  public static Capability instanceOf(final String identifier) {
    return new Capability(identifier, null, null);
  }

  public static Capability parameterInstanceOf(final String identifier, final long parameter) {
    return new Capability(identifier, parameter, parameter);
  }

  public static Capability lowerBoundInstanceOf(final String identifier, final long lowerBoundParameter) {
    return new Capability(identifier, lowerBoundParameter, null);
  }

  public static Capability upperBoundInstanceOf(final String identifier, final long upperBoundParameter) {
    return new Capability(identifier, null, upperBoundParameter);
  }

  public static Capability boundedInstanceOf(final String identifier, final long lowerBoundParameter, final long upperBoundParameter) {
    ArgumentChecker.isTrue(lowerBoundParameter <= upperBoundParameter, "lower bound must be less than upper bound");
    return new Capability(identifier, lowerBoundParameter, upperBoundParameter);
  }

  public String getIdentifier() {
    return _identifier;
  }

  public Long getLowerBoundParameter() {
    return _parameterLow;
  }

  public Long getUpperBoundParameter() {
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

}
