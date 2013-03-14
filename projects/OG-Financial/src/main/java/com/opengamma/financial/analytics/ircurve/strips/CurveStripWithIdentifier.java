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
public class CurveStripWithIdentifier implements Comparable<CurveStripWithIdentifier> {
  private final CurveStrip _strip;
  private final ExternalId _id;

  public CurveStripWithIdentifier(final CurveStrip strip, final ExternalId id) {
    ArgumentChecker.notNull(strip, "strip");
    ArgumentChecker.notNull(id, "id");
    _strip = strip;
    _id = id;
  }

  public CurveStrip getCurveStrip() {
    return _strip;
  }

  public ExternalId getIdentifier() {
    return _id;
  }

  @Override
  public int compareTo(final CurveStripWithIdentifier o) {
    final int result = _strip.compareTo(o._strip);
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
    result = prime * result + _strip.hashCode();
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
    if (!(obj instanceof CurveStripWithIdentifier)) {
      return false;
    }
    final CurveStripWithIdentifier other = (CurveStripWithIdentifier) obj;
    if (!ObjectUtils.equals(_id, other._id)) {
      return false;
    }
    if (!ObjectUtils.equals(_strip, other._strip)) {
      return false;
    }
    return true;
  }


}
