/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

public class Expiry implements InstantProvider {

  private final ZonedDateTime _expiry;
  private final ExpiryAccuracy _accuracy;

  public Expiry(final ZonedDateTime expiry) {
    _expiry = expiry;
    _accuracy = null;
  }

  public Expiry(final ZonedDateTime expiry, final ExpiryAccuracy accuracy) {
    _expiry = expiry;
    _accuracy = accuracy;
  }

  public ExpiryAccuracy getAccuracy() {
    return _accuracy;
  }

  // we probably don't need this.
  public ZonedDateTime getExpiry() {
    return _expiry;
  }

  @Override
  public Instant toInstant() {
    return _expiry.toInstant();
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Expiry))
      return false;
    final Expiry other = (Expiry) o;
    return ObjectUtils.equals(other.getAccuracy(), getAccuracy()) && other.getExpiry().equals(getExpiry());
  }

  @Override
  public int hashCode() {
    return (_accuracy != null ? _accuracy.hashCode() : 0) ^ _expiry.hashCode();
  }

  @Override
  public String toString() {
    if (_accuracy != null)
      return "Expiry[" + _expiry + " accuracy " + _accuracy + "]";
    else
      return "Expiry[" + _expiry + "]";
  }
}
