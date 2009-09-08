package com.opengamma.financial.securities.descriptions;

public interface DescriptionVisitor<T> {
  public T visitEquity(EquityDescription equityDescription);
  public T visitIndex(IndexDescription indexDescription);
  public T visitFX(FXDescription fxDescription);
}
