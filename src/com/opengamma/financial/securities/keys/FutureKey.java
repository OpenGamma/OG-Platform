package com.opengamma.financial.securities.keys;

import com.opengamma.util.time.Expiry;

public class FutureKey implements SecurityKey {
  private SecurityKey _underlying;
  private Expiry _expiry;

  public FutureKey(SecurityKey underlying, Expiry expiry) {
    _underlying = underlying;
    _expiry = expiry;
  }
  
  public SecurityKey getUnderlying() {
    return _underlying;
  }
  
  public Expiry getExpiry() {
    return _expiry;
  }
    
  public String toString() {
    return "FutureKey["+getUnderlying()+" on "+getExpiry()+"]";
  }
  
  public int hashCode() {
    return getUnderlying().hashCode() ^ getExpiry().hashCode();
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof FutureKey)) {
      return false;
    }
    FutureKey other = (FutureKey)o;
    return _underlying.equals(other.getUnderlying()) && _expiry.equals(other.getExpiry());
  }
  
  public <T> T accept(SecurityKeyVisitor<T> visitor) {
    return visitor.visitFutureKey(this);
  }
}
