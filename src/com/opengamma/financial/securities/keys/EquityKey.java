package com.opengamma.financial.securities.keys;

public class EquityKey extends EquityOrEquivalentKey {
  
  public EquityKey(String ticker) {
    super(ticker);
  }
    
  public String toString() {
    return "EquityKey["+getTicker()+"]";
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof EquityKey)) {
      return false;
    }
    EquityKey other = (EquityKey)o;
    return getTicker().equals(other.getTicker());
  }
  
  public <T> T accept(SecurityKeyVisitor<T> visitor) {
    return visitor.visitEquityKey(this);
  }
}
