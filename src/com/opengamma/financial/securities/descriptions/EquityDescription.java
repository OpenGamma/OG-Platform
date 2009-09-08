package com.opengamma.financial.securities.descriptions;

import com.opengamma.financial.securities.Exchange;
import com.opengamma.financial.securities.keys.EquityKey;
import com.opengamma.util.CompareUtils;

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
    if (!CompareUtils.equalsWithNull(getSymbol(), other.getSymbol())) {
      return false;
    }
    if (!CompareUtils.equalsWithNull(getName(), other.getName())) {
      return false;
    }
    return CompareUtils.equalsWithNull(getExchange(), other.getExchange());
  }
}
