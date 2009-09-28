package com.opengamma.financial.securities.keys;

// TODO: Move some of this stuff to a common base class with EquityKey?
public class IndexKey extends ExchangeTradedKey {

  public IndexKey(String ticker) {
    super(ticker);
  }
  
  public String toString() {
    return "IndexKey["+getTicker()+"]";
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof IndexKey)) {
      return false;
    }
    IndexKey other = (IndexKey)o;
    return getTicker().equals(other.getTicker());
  }
  
  public <T> T accept(SecurityKeyVisitor<T> visitor) {
    return visitor.visitIndexKey(this);
  }
}
