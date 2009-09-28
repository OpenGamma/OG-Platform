package com.opengamma.financial.securities.keys;

public class IndexOptionKey extends ExchangeTradedKey {
  
  public IndexOptionKey(String ticker) {
    super(ticker);
  }
    
  public String toString() {
    return "IndexOptionKey["+getTicker()+"]";
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof IndexOptionKey)) {
      return false;
    }
    IndexOptionKey other = (IndexOptionKey)o;
    return getTicker().equals(other.getTicker());
  }
  
  public <T> T accept(SecurityKeyVisitor<T> visitor) {
    return visitor.visitIndexOptionKey(this);
  }
}
