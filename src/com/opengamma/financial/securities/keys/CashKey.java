package com.opengamma.financial.securities.keys;

import com.opengamma.financial.securities.Currency;

public class CashKey implements SecurityKey {
  private Currency _currency;

  public CashKey(Currency currency, Tenor period, CashRateType type) {
    _currency = currency;
  }
  
  public Currency getCurrency() {
    return _currency;
  }
    
  public String toString() {
    return "CashKey["+getCurrency()+"]";
  }
  
  public int hashCode() {
    return getCurrency().hashCode();
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof CashKey)) {
      return false;
    }
    CashKey other = (CashKey)o;
    return _currency.equals(other.getCurrency());
  }
  
  public <T> T accept(SecurityKeyVisitor<T> visitor) {
    return visitor.visitCashKey(this);
  }
}
