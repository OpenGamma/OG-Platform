package com.opengamma.financial.securities.keys;

public class EquityOptionKey extends ExchangeTradedKey {
  
  public EquityOptionKey(String ticker) {
    super(ticker);
  }
    
  public String toString() {
    return "EquityOptionKey["+getTicker()+"]";
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof EquityOptionKey)) {
      return false;
    }
    EquityOptionKey other = (EquityOptionKey)o;
    return getTicker().equals(other.getTicker());
  }
  
  public <T> T accept(SecurityKeyVisitor<T> visitor) {
    return visitor.visitEquityOptionKey(this);
  }
}
