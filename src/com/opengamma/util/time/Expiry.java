package com.opengamma.util.time;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.apache.commons.lang.ObjectUtils;

public class Expiry implements InstantProvider {

  private final InstantProvider _expiry;
  private final ExpiryAccuracy _accuracy;
  
  public Expiry(InstantProvider expiry) {
    _expiry = expiry;
    _accuracy = null;
  }
  
  public Expiry(InstantProvider expiry, ExpiryAccuracy accuracy) {
    _expiry = expiry;
    _accuracy = accuracy;
  }
  
  public ExpiryAccuracy getAccuracy() {
    return _accuracy;
  }
  // we probably don't need this.
  public InstantProvider getExpiry() {
    return _expiry;
  }
  
  @Override
  public Instant toInstant() {
    return _expiry.toInstant();
  }

  public boolean equals(Object o) {
    if (!(o instanceof Expiry)) {
      return false;
    }
    Expiry other = (Expiry)o;
    return (ObjectUtils.equals(other.getAccuracy(),getAccuracy())) && 
           (other.getExpiry().equals(getExpiry()));
  }
  
  public int hashCode() {
    return (_accuracy != null ?_accuracy.hashCode() : 0) ^ _expiry.hashCode();
  }
  public String toString() {
    if (_accuracy != null) {
      return "Expiry["+_expiry+" accuracy "+_accuracy+"]";
    } else {
      return "Expiry["+_expiry+"]";
    }
  }
}
