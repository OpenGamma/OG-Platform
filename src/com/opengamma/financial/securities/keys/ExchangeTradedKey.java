package com.opengamma.financial.securities.keys;

public abstract class ExchangeTradedKey implements SecurityKey {
  private String _ticker;

  public ExchangeTradedKey(String ticker) {
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
    if (!(o instanceof ExchangeTradedKey)) {
      return false;
    }
    ExchangeTradedKey other = (ExchangeTradedKey)o;
    return _ticker.equals(other.getTicker());
  }
}
