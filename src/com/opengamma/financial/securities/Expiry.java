package com.opengamma.financial.securities;

import javax.time.Instant;
import javax.time.InstantProvider;

public class Expiry implements InstantProvider {

  private InstantProvider _expiry;
  private ExpiryAccuracy _accuracy;
  
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
    return (other.getAccuracy().equals(getAccuracy())) && 
           (other.getExpiry().equals(getExpiry()));
  }
  
  public int hashCode() {
    return _accuracy.hashCode() ^ _expiry.hashCode();
  }
  public String toString() {
    return "Expiry["+_expiry+" accuracy "+_accuracy+"]";
  }
}
