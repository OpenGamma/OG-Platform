package com.opengamma.financial.securities.descriptions;

import com.opengamma.financial.securities.Exchange;
import com.opengamma.financial.securities.keys.IndexKey;
import com.opengamma.util.CompareUtils;

public class IndexDescription extends EquityOrEquivalentDescription<IndexKey> {
  
  public IndexDescription(String symbol, String name, Exchange exchange) {
    super(symbol, name, exchange);
  }

  public <T> T accept(DescriptionVisitor<T> visitor) {
    return visitor.visitIndex(this);
  }
  
  public IndexKey toKey() {
    return new IndexKey(getSymbol());
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof IndexDescription)) {
      return false;
    }
    IndexDescription other = (IndexDescription) o;
    if (!CompareUtils.equalsWithNull(getSymbol(), other.getSymbol())) {
      return false;
    }
    if (!CompareUtils.equalsWithNull(getName(), other.getName())) {
      return false;
    }
    return CompareUtils.equalsWithNull(getExchange(), other.getExchange());
  }
}
