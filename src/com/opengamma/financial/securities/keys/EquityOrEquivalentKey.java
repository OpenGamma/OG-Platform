package com.opengamma.financial.securities.keys;

public abstract class EquityOrEquivalentKey implements SecurityKey {
  private String _ticker;

  public EquityOrEquivalentKey(String ticker) {
    _ticker = ticker;
  }
  
  public String getTicker() {
    return _ticker;
  }
  
  public abstract String toString();
  
  public int hashCode() {
    return _ticker.hashCode();
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof EquityOrEquivalentKey)) {
      return false;
    }
    EquityOrEquivalentKey other = (EquityOrEquivalentKey)o;
    return _ticker.equals(other.getTicker());
  }
}
