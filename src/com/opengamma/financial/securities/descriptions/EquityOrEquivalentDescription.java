package com.opengamma.financial.securities.descriptions;

import com.opengamma.financial.securities.Exchange;


public abstract class EquityOrEquivalentDescription<E> implements Description<E> {
  private String _symbol;
  private String _name;
  private Exchange _exchange;
  
  public EquityOrEquivalentDescription(String symbol, String name, Exchange exchange) {
    _symbol = symbol;
    _name = name;
    _exchange = exchange;
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

  public abstract <T> T accept(DescriptionVisitor<T> visitor);
  
  public abstract E toKey();
  
  public int hashCode() {
    return getSymbol().hashCode();
  }
}
