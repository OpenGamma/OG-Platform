package com.opengamma.financial.securities.keys;

import com.opengamma.financial.securities.Currency;

public class FXKey implements SecurityKey {
  private Currency _numerator;
  private Currency _denominator;

  public FXKey(Currency numerator, Currency denominator) {
    _numerator = numerator;
    _denominator = denominator;
  }
  
  public Currency getNumerator() {
    return _numerator;
  }
  
  public Currency getDenominator() {
    return _denominator;
  }
  
  public String toString() {
    return "FXKey["+getNumerator()+"/"+getDenominator()+"]";
  }
  
  public int hashCode() {
    return _numerator.hashCode() ^ _denominator.hashCode();
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof FXKey)) {
      return false;
    }
    FXKey other = (FXKey)o;
    return _numerator.equals(other.getNumerator()) && _denominator.equals(other.getDenominator());
  }
  
  public <T> T accept(SecurityKeyVisitor<T> visitor) {
    return visitor.visitFXKey(this);
  }
}
