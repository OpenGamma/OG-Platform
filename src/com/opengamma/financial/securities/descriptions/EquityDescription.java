package com.opengamma.financial.securities.descriptions;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.securities.Exchange;
import com.opengamma.financial.securities.keys.EquityKey;

public class EquityDescription extends EquityOrEquivalentDescription<EquityKey> {
  
  public EquityDescription(String symbol, String name, Exchange exchange) {
    super(symbol, name, exchange);
  }

  public <T> T accept(DescriptionVisitor<T> visitor) {
    return visitor.visitEquity(this);
  }
  
  public EquityKey toKey() {
    return new EquityKey(getSymbol());
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof EquityDescription)) {
      return false;
    }
    EquityDescription other = (EquityDescription) o;
    if (!ObjectUtils.equals(getSymbol(), other.getSymbol())) {
      return false;
    }
    if (!ObjectUtils.equals(getName(), other.getName())) {
      return false;
    }
    return ObjectUtils.equals(getExchange(), other.getExchange());
  }
}
