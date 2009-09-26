package com.opengamma.financial.securities.descriptions;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.securities.Currency;
import com.opengamma.financial.securities.Exchange;
import com.opengamma.financial.securities.keys.FXKey;
import com.opengamma.util.CompareUtils;

public class FXDescription implements Description<FXKey> {
  private String _symbol;
  private String _name;
  private Exchange _exchange;
  private Currency _numerator;
  private Currency _denominator;
  
  public FXDescription(String symbol, String name, Exchange exchange) {
    CompareUtils.checkForNull(symbol);
    CompareUtils.checkForNull(name);
    CompareUtils.checkForNull(exchange);
    _symbol = symbol;
    _name = name;
    _exchange = exchange;
    _numerator = Currency.getInstance(symbol.substring(0, 3));
    CompareUtils.checkForNull(_numerator); // check we get it correct
    _denominator = Currency.getInstance(symbol.substring(3, 6));
    CompareUtils.checkForNull(_denominator); // check we get it correct
  }

  public String getSymbol() {
    return _symbol;
  }

  public String getName() {
    return _name;
  }

  public Exchange getExchange() {
    return _exchange;
  }

  public <T> T accept(DescriptionVisitor<T> visitor) {
    return visitor.visitFX(this);
  }
  
  public FXKey toKey() {
    return new FXKey(_numerator, _denominator);
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof FXDescription)) {
      return false;
    }
    FXDescription other = (FXDescription) o;
    if (!ObjectUtils.equals(getSymbol(), other.getSymbol())) {
      return false;
    }
    if (!ObjectUtils.equals(getName(), other.getName())) {
      return false;
    }
    return ObjectUtils.equals(getExchange(), other.getExchange());
  }
  
  public int hashCode() {
    return getSymbol().hashCode();
  }
}
