package com.opengamma.financial.securities.descriptions;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.securities.Exchange;
import com.opengamma.financial.securities.keys.IndexKey;

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
    if (!ObjectUtils.equals(getSymbol(), other.getSymbol())) {
      return false;
    }
    if (!ObjectUtils.equals(getName(), other.getName())) {
      return false;
    }
    return ObjectUtils.equals(getExchange(), other.getExchange());
  }
}
